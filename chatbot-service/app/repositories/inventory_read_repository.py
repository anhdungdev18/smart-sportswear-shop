from __future__ import annotations

from app.db.pool import get_pool
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)


async def get_available_stock(product_ids: list[str]) -> dict[str, int]:
    """
    Returns {product_id: total_available_units} for the given product IDs.
    The main product_repository query already aggregates this; use this for
    spot-checks or enrichment outside the main search flow.
    """
    pool = get_pool()
    if pool is None or not product_ids:
        return {}

    sql = """
        SELECT
            product_id::text,
            COALESCE(SUM(GREATEST(stock_quantity - reserved_quantity, 0)), 0)::int AS available
        FROM product_variants
        WHERE product_id = ANY($1::uuid[])
          AND status = 'ACTIVE'
        GROUP BY product_id
    """
    async with pool.acquire() as conn:
        rows = await conn.fetch(sql, product_ids)

    return {str(row["product_id"]): row["available"] for row in rows}
