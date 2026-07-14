from __future__ import annotations

from app.retrieval.product.filters.product_filter import ProductFilter
from app.repositories import product_repository
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)


async def retrieve(f: ProductFilter) -> list[dict]:
    """
    Keyword retrieval: delegates to product_repository with the structured filter.
    Returns raw DB row dicts.
    """
    rows = await product_repository.keyword_search(f)
    logger.info(f"keyword_retriever | hits={len(rows)} filter={_summarize(f)}")
    return rows


def _summarize(f: ProductFilter) -> str:
    parts = []
    if f.product_type:
        parts.append(f"type={f.product_type}")
    if f.sport_type_hint:
        parts.append(f"sport={f.sport_type_hint}")
    if f.gender:
        parts.append(f"gender={f.gender}")
    if f.price_min:
        parts.append(f"price_min={f.price_min}")
    if f.price_max:
        parts.append(f"price_max={f.price_max}")
    if f.keyword:
        parts.append(f"kw={f.keyword[:30]}")
    return ",".join(parts) or "no-filter"
