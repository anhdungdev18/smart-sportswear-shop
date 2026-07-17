"""
Catalog context service — tự động generate từ DB, cache vào file.

Giống DigiAI's catalog_context.py:
  - refresh_catalog_context() → query DB, ghi cache file
  - get_catalog_context()     → lazy-load từ file hoặc DB nếu file chưa có

Cache file nằm cạnh catalog.md (query_rewrite/).
Gọi refresh_catalog_context() lúc app startup để luôn đồng bộ với DB.
"""
from __future__ import annotations

import logging
from pathlib import Path

logger = logging.getLogger(__name__)

_CACHE_FILE = (
    Path(__file__).parent.parent
    / "retrieval" / "product" / "query_rewrite" / "catalog_context.txt"
)

_CATEGORY_SQL = """
    SELECT c.name, COUNT(p.id) AS cnt
    FROM categories c
    LEFT JOIN products p ON p.category_id = c.id AND p.status = 'ACTIVE'
    WHERE c.status = 'ACTIVE'
    GROUP BY c.name
    HAVING COUNT(p.id) > 0
    ORDER BY c.name
"""

_BRAND_SQL = """
    SELECT DISTINCT b.name FROM brands b
    JOIN products p ON p.brand_id = b.id AND p.status = 'ACTIVE'
    ORDER BY b.name
"""

_PRODUCT_SAMPLE_SQL = """
    SELECT DISTINCT p.name FROM products p
    WHERE p.status = 'ACTIVE'
    ORDER BY p.name
    LIMIT 60
"""


async def _query_catalog() -> str:
    """Async DB query để build catalog context string."""
    try:
        from app.db.pool import get_pool
        pool = get_pool()
        if pool is None:
            return ""

        async with pool.acquire() as conn:
            categories = await conn.fetch(_CATEGORY_SQL)
            brands     = await conn.fetch(_BRAND_SQL)
            products   = await conn.fetch(_PRODUCT_SAMPLE_SQL)

    except Exception as exc:
        logger.warning(f"catalog_context | DB query failed: {exc!r}")
        return ""

    lines: list[str] = ["## Danh mục sản phẩm của cửa hàng Dung Quyền Hải Sport"]
    if categories:
        lines.append("### Danh mục:")
        for row in categories:
            lines.append(f"- {row['name']} ({row['cnt']} sản phẩm)")

    if brands:
        lines.append(f"\n### Thương hiệu: {', '.join(r['name'] for r in brands)}")

    if products:
        lines.append("\n### Ví dụ sản phẩm thực tế:")
        for row in products:
            lines.append(f"- {row['name']}")

    return "\n".join(lines)


async def refresh_catalog_context() -> str:
    """Force regenerate từ DB và ghi cache file. Gọi lúc app startup."""
    content = await _query_catalog()
    if content:
        try:
            _CACHE_FILE.write_text(content, encoding="utf-8")
            logger.info(f"catalog_context | refreshed {len(content)} chars → {_CACHE_FILE.name}")
        except Exception as exc:
            logger.warning(f"catalog_context | cannot write cache: {exc!r}")
    return content


def get_catalog_context() -> str:
    """Sync lazy-load từ cache file. Trả về empty string nếu chưa có."""
    if _CACHE_FILE.exists():
        try:
            return _CACHE_FILE.read_text(encoding="utf-8")
        except Exception:
            pass
    return ""
