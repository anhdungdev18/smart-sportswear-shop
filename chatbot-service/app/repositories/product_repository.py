from __future__ import annotations

from app.db.pool import get_pool
from app.retrieval.product.filters.product_filter import ProductFilter
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

# Base SELECT — always the same; WHERE is built dynamically
_BASE_SELECT = """
    SELECT
        p.id::text                          AS product_id,
        p.name,
        p.slug,
        p.short_description,
        p.gender,
        p.sport_type,
        p.product_type,
        c.name                              AS category_name,
        b.name                              AS brand_name,
        MIN(pv.price)::float                AS price_min,
        MAX(pv.price)::float                AS price_max,
        COALESCE(
            array_agg(DISTINCT pv.color)
            FILTER (WHERE pv.status = 'ACTIVE'
                      AND (pv.stock_quantity - pv.reserved_quantity) > 0),
            ARRAY[]::varchar[]
        )                                   AS available_colors,
        COALESCE(
            array_agg(DISTINCT pv.size)
            FILTER (WHERE pv.status = 'ACTIVE'
                      AND (pv.stock_quantity - pv.reserved_quantity) > 0),
            ARRAY[]::varchar[]
        )                                   AS available_sizes,
        COALESCE(
            SUM(GREATEST(pv.stock_quantity - pv.reserved_quantity, 0)),
            0
        )::int                              AS total_available,
        (
            SELECT pi2.image_url
            FROM product_images pi2
            WHERE pi2.product_id = p.id
            ORDER BY pi2.is_primary DESC, pi2.sort_order ASC
            LIMIT 1
        )                                   AS primary_image
    FROM products p
    JOIN categories c ON c.id = p.category_id
    JOIN brands b     ON b.id = p.brand_id
    JOIN product_variants pv ON pv.product_id = p.id
"""

_GROUP_BY = """
    GROUP BY
        p.id, p.name, p.slug, p.short_description,
        p.gender, p.sport_type, p.product_type,
        c.name, b.name
    HAVING COALESCE(SUM(GREATEST(pv.stock_quantity - pv.reserved_quantity, 0)), 0) > 0
    ORDER BY p.name
"""


def _build_where(f: ProductFilter) -> tuple[str, list]:
    """Return (where_clause, params_list). Params are positional ($1, $2, ...)."""
    conditions: list[str] = ["p.status = 'ACTIVE'", "c.status = 'ACTIVE'"]
    params: list = []
    idx = 1  # asyncpg positional param index

    if f.product_type:
        # NULL product_type means "untyped" in this catalog (half the products,
        # incl. all footwear, have no product_type set). Excluding them would
        # zero out any query that infers a type from words like "áo"/"giày",
        # so treat NULL as a match rather than a mismatch.
        params.append(f.product_type)
        conditions.append(f"(p.product_type = ${idx} OR p.product_type IS NULL)")
        idx += 1

    if f.gender:
        params.append(f.gender)
        # Include UNISEX products when specific gender requested
        conditions.append(f"(p.gender = ${idx} OR p.gender = 'UNISEX' OR p.gender IS NULL)")
        idx += 1

    if f.sport_type_hint:
        params.append(f"%{f.sport_type_hint}%")
        # sport_type is NULL for most products; fall back to category name ILIKE.
        # Also check reversed word order: "bóng đá" ↔ "đá bóng" in category names.
        hint_words = f.sport_type_hint.split()
        sport_conds = [f"p.sport_type ILIKE ${idx}", f"c.name ILIKE ${idx}"]
        idx += 1
        if len(hint_words) == 2:
            params.append(f"%{hint_words[1]} {hint_words[0]}%")
            sport_conds.append(f"c.name ILIKE ${idx}")
            idx += 1
        conditions.append(f"({' OR '.join(sport_conds)})")

    if f.price_min is not None:
        params.append(f.price_min)
        conditions.append(f"pv.price >= ${idx}")
        idx += 1

    if f.price_max is not None:
        params.append(f.price_max)
        conditions.append(f"pv.price <= ${idx}")
        idx += 1

    if f.brand:
        params.append(f.brand)
        conditions.append(f"b.name = ${idx}")
        idx += 1

    if f.color:
        params.append(f"%{f.color}%")
        conditions.append(f"pv.color ILIKE ${idx}")
        idx += 1

    # Gender/sport words handled by structural filters; search verbs never appear in product names.
    # The tool often passes a whole user sentence as the query ("shop có áo Real Madrid
    # không?"), so filler/question words must be dropped or one non-name word ("không")
    # AND-fails the whole keyword branch.
    _KW_STOP = frozenset([
        # gender
        "nam", "nữ", "men", "women", "male", "female", "unisex",
        # Vietnamese search/buy verbs that users prefix to queries
        "tìm", "mua", "cần", "muốn", "có", "bán", "xem", "cho", "shop",
        "tìm kiếm", "mua sắm",
        # question / filler / connective words common in full-sentence queries
        "không", "nào", "gì", "vậy", "thế", "ạ", "à", "hả", "nhé", "nha", "ơi",
        "cửa", "hàng", "sản", "phẩm", "loại", "kiểu", "mẫu", "cái", "chiếc", "đôi",
        "của", "và", "hay", "hoặc", "với", "mình", "bạn", "em", "anh", "chị",
        "này", "đó", "kia", "ấy", "được", "còn", "về", "giúp", "ạ", "cho",
        # request / consult verbs and price fillers
        "tư", "vấn", "gợi", "giới", "thiệu", "tham", "khảo", "hỏi", "coi", "kiếm",
        "giá", "bao", "nhiêu", "đang", "hiện", "shop",
        # descriptor adjectives / quantifiers that never appear in product names
        "đấu", "đẹp", "xịn", "chất", "ngon", "hot", "đỉnh", "nhất", "tốt",
        "phù", "hợp", "vài", "mấy", "một", "chút", "ít", "nào", "đó",
    ])

    # Keyword search: each word must appear in at least one text field (AND across words)
    if f.keyword:
        words = [
            w.strip("?!.,;:\"'()[]…") for w in f.keyword.split()
        ]
        words = [w for w in words if len(w) > 1 and w not in _KW_STOP]
        word_clauses: list[str] = []
        for w in words:
            kw = f"%{w}%"
            params.append(kw)
            p = f"${idx}"
            word_clauses.append(
                f"(p.name ILIKE {p} OR p.short_description ILIKE {p} "
                f"OR c.name ILIKE {p} OR b.name ILIKE {p})"
            )
            idx += 1
        if word_clauses:
            conditions.append(f"({' AND '.join(word_clauses)})")

    for feat in f.feature_hints:
        params.append(f"%{feat}%")
        p = f"${idx}"
        conditions.append(f"(p.name ILIKE {p} OR p.short_description ILIKE {p})")
        idx += 1

    where_clause = " AND ".join(conditions)
    return where_clause, params


async def keyword_search(f: ProductFilter) -> list[dict]:
    """
    Run keyword + structured-filter search. Returns list of raw row dicts.
    Returns [] if pool is not configured.
    """
    pool = get_pool()
    if pool is None:
        logger.warning("product_repository | DB pool not configured — returning empty")
        return []

    where_clause, params = _build_where(f)
    params.append(f.limit)
    limit_param = f"${len(params)}"

    sql = f"{_BASE_SELECT} WHERE {where_clause} {_GROUP_BY} LIMIT {limit_param}"

    logger.debug(f"product_repository | sql_params={len(params)}")

    async with pool.acquire() as conn:
        rows = await conn.fetch(sql, *params)

    return [dict(row) for row in rows]


async def find_variants_by_sku(sku: str, *, exact: bool, limit: int = 10) -> list[dict]:
    """Lookup variants by SKU. Exact lookup is case-insensitive; partial is explicit fallback."""
    pool = get_pool()
    if pool is None:
        return []

    operator = "= UPPER($1)" if exact else "LIKE '%' || UPPER($1) || '%'"
    sql = f"""
        SELECT
            p.id::text AS product_id,
            pv.id::text AS variant_id,
            p.name,
            p.slug,
            pv.sku,
            c.name AS category_name,
            b.name AS brand_name,
            pv.color,
            pv.size,
            pv.price::float AS price,
            GREATEST(pv.stock_quantity - pv.reserved_quantity, 0)::int AS available,
            pv.status AS variant_status,
            (
                SELECT pi.image_url
                FROM product_images pi
                WHERE pi.product_id = p.id
                ORDER BY pi.is_primary DESC, pi.sort_order ASC
                LIMIT 1
            ) AS primary_image
        FROM product_variants pv
        JOIN products p ON p.id = pv.product_id
        JOIN categories c ON c.id = p.category_id
        JOIN brands b ON b.id = p.brand_id
        WHERE UPPER(pv.sku) {operator}
          AND p.status = 'ACTIVE'
          AND c.status = 'ACTIVE'
          AND pv.status != 'INACTIVE'
        ORDER BY CASE WHEN UPPER(pv.sku) = UPPER($1) THEN 0 ELSE 1 END, pv.sku
        LIMIT $2
    """
    async with pool.acquire() as conn:
        rows = await conn.fetch(sql, sku, limit)
    return [dict(row) for row in rows]


# ── Phase 6: product detail queries ──────────────────────────────────────────

async def get_product_by_id(product_id: str) -> dict | None:
    """Return basic product row or None. product_id is a UUID string."""
    pool = get_pool()
    if pool is None:
        return None

    sql = """
        SELECT
            p.id,
            p.name,
            p.slug,
            p.short_description,
            p.gender,
            p.sport_type,
            p.product_type,
            c.name AS category_name,
            b.name AS brand_name
        FROM products p
        JOIN categories c ON c.id = p.category_id
        JOIN brands     b ON b.id = p.brand_id
        WHERE p.id = $1
          AND p.status = 'ACTIVE'
          AND c.status = 'ACTIVE'
    """
    async with pool.acquire() as conn:
        row = await conn.fetchrow(sql, product_id)
    return dict(row) if row else None


async def get_product_variants(product_id: str) -> list[dict]:
    """Return all non-INACTIVE variants, ordered by price/size/color."""
    pool = get_pool()
    if pool is None:
        return []

    sql = """
        SELECT
            pv.id             AS variant_id,
            pv.sku,
            pv.color,
            pv.size,
            pv.price,
            pv.stock_quantity,
            pv.reserved_quantity,
            pv.status
        FROM product_variants pv
        WHERE pv.product_id = $1
          AND pv.status != 'INACTIVE'
        ORDER BY pv.price ASC, pv.size ASC, pv.color ASC
    """
    async with pool.acquire() as conn:
        rows = await conn.fetch(sql, product_id)
    return [dict(row) for row in rows]


async def get_product_images(product_id: str) -> list[str]:
    """Return image URLs ordered by is_primary DESC, sort_order ASC."""
    pool = get_pool()
    if pool is None:
        return []

    sql = """
        SELECT pi.image_url
        FROM product_images pi
        WHERE pi.product_id = $1
        ORDER BY pi.is_primary DESC, pi.sort_order ASC
    """
    async with pool.acquire() as conn:
        rows = await conn.fetch(sql, product_id)
    return [row["image_url"] for row in rows if row["image_url"]]


async def find_related_products(product_id: str, limit: int = 4) -> list[dict]:
    """
    Phase 7: Find related products by same category > same sport_type > same brand.
    Excludes the source product. Returns in-stock products only.
    Ranking: 1 = same category, 2 = same sport_type, 3 = same brand.
    """
    pool = get_pool()
    if pool is None:
        return []

    sql = """
        WITH base AS (
            SELECT p.category_id, p.brand_id, p.sport_type, p.gender
            FROM products p
            WHERE p.id::text = $1
        )
        SELECT
            p.id::text                          AS product_id,
            p.name,
            p.slug,
            c.name                              AS category_name,
            b.name                              AS brand_name,
            p.sport_type,
            p.gender,
            MIN(pv.price)::float                AS price_min,
            (
                SELECT pi2.image_url
                FROM product_images pi2
                WHERE pi2.product_id = p.id
                ORDER BY pi2.is_primary DESC, pi2.sort_order ASC
                LIMIT 1
            )                                   AS primary_image,
            CASE
                WHEN p.category_id = (SELECT category_id FROM base)                          THEN 1
                WHEN p.sport_type  = (SELECT sport_type  FROM base)
                 AND (SELECT sport_type FROM base) IS NOT NULL                                THEN 2
                ELSE 3
            END                                 AS relevance_rank
        FROM products p
        JOIN categories c ON c.id = p.category_id
        JOIN brands     b ON b.id = p.brand_id
        JOIN product_variants pv ON pv.product_id = p.id
        WHERE p.status  = 'ACTIVE'
          AND c.status  = 'ACTIVE'
          AND p.id::text != $1
          AND (
              p.category_id = (SELECT category_id FROM base)
              OR (
                  p.sport_type = (SELECT sport_type FROM base)
                  AND (SELECT sport_type FROM base) IS NOT NULL
              )
              OR p.brand_id = (SELECT brand_id FROM base)
          )
          AND (
              (SELECT gender FROM base) IS NULL
              OR (SELECT gender FROM base) = 'UNISEX'
              OR p.gender = (SELECT gender FROM base)
              OR p.gender = 'UNISEX'
              OR p.gender IS NULL
          )
        GROUP BY
            p.id, p.name, p.slug, c.name, b.name,
            p.sport_type, p.gender, p.category_id, p.brand_id
        HAVING COALESCE(SUM(GREATEST(pv.stock_quantity - pv.reserved_quantity, 0)), 0) > 0
        ORDER BY relevance_rank ASC, p.name ASC
        LIMIT $2
    """
    async with pool.acquire() as conn:
        rows = await conn.fetch(sql, product_id, limit)
    return [dict(row) for row in rows]
