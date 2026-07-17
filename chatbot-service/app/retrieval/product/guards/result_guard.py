from __future__ import annotations

from app.retrieval.product.filters.product_filter import ProductFilter
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)


def apply_guards(items: list[dict], f: ProductFilter) -> list[dict]:
    """
    Post-retrieval hard guards. SQL handles most cases; this is the safety net.

    Guards applied (Phase 3):
    - active/stock guard: remove items with totalAvailable == 0
    - product_type guard: if filter specifies type, remove mismatches (unless DB returned them somehow)
    - gender guard: if filter specifies MEN/WOMEN, remove items that are the opposite gender
    """
    result = items

    # 1. Stock guard (paranoia — SQL HAVING already filters this)
    before = len(result)
    result = [i for i in result if i["total_available"] > 0]
    if len(result) < before:
        logger.info(f"result_guard | stock_guard removed {before - len(result)} items")

    # 2. Product type guard
    if f.product_type:
        before = len(result)
        result = [i for i in result if i.get("product_type") == f.product_type]
        removed = before - len(result)
        if removed:
            logger.info(f"result_guard | type_guard removed {removed} non-{f.product_type} items")

    # 3. Gender guard (only remove clear opposites, keep UNISEX and None)
    if f.gender in ("MEN", "WOMEN"):
        opposite = "WOMEN" if f.gender == "MEN" else "MEN"
        before = len(result)
        result = [i for i in result if i.get("gender") != opposite]
        removed = before - len(result)
        if removed:
            logger.info(f"result_guard | gender_guard removed {removed} {opposite} items")

    return result
