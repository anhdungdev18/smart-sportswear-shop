from __future__ import annotations

from app.db.pool import get_pool
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

# Embedding model: OpenAI text-embedding-3-small (1536 dimensions)
# The `product_embeddings` table schema (V14 migration):
#   product_id UUID PK, embedding vector(1536), document_text TEXT, updated_at TIMESTAMPTZ
#   IVFFlat index with lists=20 (cosine ops)


async def vector_search(
    query_text: str,
    limit: int = 10,
    gender: str | None = None,
    product_type: str | None = None,
    sport_type_hint: str | None = None,
    price_min: float | None = None,
    price_max: float | None = None,
    brand: str | None = None,
    color: str | None = None,
) -> list[dict]:
    """
    Similarity search against product_embeddings using pgvector.

    Returns rows in the same shape as keyword_search() so RRF fusion can merge them.
    Returns [] when: pool not ready, model not loaded, table missing, or any other error.
    """
    pool = get_pool()
    if pool is None:
        logger.info("vector_repository | pool not configured, skipping")
        return []

    from app.services import embedder

    embedding = await embedder.embed(query_text)
    if embedding is None:
        return []

    embedding_str = f"[{','.join(str(v) for v in embedding)}]"

    conditions: list[str] = ["p.status = 'ACTIVE'", "c.status = 'ACTIVE'"]
    params: list = [embedding_str, limit]
    idx = 3

    if product_type:
        params.append(product_type)
        conditions.append(f"p.product_type = ${idx}")
        idx += 1

    if gender and gender in ("MEN", "WOMEN"):
        params.append(gender)
        conditions.append(f"(p.gender = ${idx} OR p.gender = 'UNISEX' OR p.gender IS NULL)")
        idx += 1

    if sport_type_hint:
        # sport_type is NULL for most products; fall back to category name ILIKE.
        # Mirror keyword repo: also check reversed word order ("bóng đá" ↔ "đá bóng").
        params.append(f"%{sport_type_hint}%")
        hint_words = sport_type_hint.split()
        sport_conds = [f"p.sport_type ILIKE ${idx}", f"c.name ILIKE ${idx}"]
        idx += 1
        if len(hint_words) == 2:
            params.append(f"%{hint_words[1]} {hint_words[0]}%")
            sport_conds.append(f"c.name ILIKE ${idx}")
            idx += 1
        conditions.append(f"({' OR '.join(sport_conds)})")

    # Price constraints — mirror keyword repo (filter on variant price before aggregation)
    if price_min is not None:
        params.append(price_min)
        conditions.append(f"pv.price >= ${idx}")
        idx += 1

    if price_max is not None:
        params.append(price_max)
        conditions.append(f"pv.price <= ${idx}")
        idx += 1

    if brand:
        params.append(brand)
        conditions.append(f"b.name = ${idx}")
        idx += 1

    if color:
        params.append(f"%{color}%")
        conditions.append(f"pv.color ILIKE ${idx}")
        idx += 1

    where_clause = " AND ".join(conditions)

    sql = f"""
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
            )                                   AS primary_image,
            1 - (pe.embedding <=> $1::vector)   AS vector_score
        FROM product_embeddings pe
        JOIN products p          ON p.id  = pe.product_id
        JOIN categories c        ON c.id  = p.category_id
        JOIN brands     b        ON b.id  = p.brand_id
        JOIN product_variants pv ON pv.product_id = p.id
        WHERE {where_clause}
        GROUP BY
            p.id, p.name, p.slug, p.short_description,
            p.gender, p.sport_type, p.product_type,
            c.name, b.name, pe.embedding
        HAVING COALESCE(SUM(GREATEST(pv.stock_quantity - pv.reserved_quantity, 0)), 0) > 0
        ORDER BY pe.embedding <=> $1::vector
        LIMIT $2
    """

    try:
        async with pool.acquire() as conn:
            rows = await conn.fetch(sql, *params)
        result = [dict(row) for row in rows]
        logger.info(f"vector_repository | hits={len(result)} query={query_text[:40]!r}")
        return result
    except Exception as exc:
        logger.warning(f"vector_repository | query_error={exc!r} — falling back to keyword-only")
        return []
