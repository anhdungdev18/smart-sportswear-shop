from __future__ import annotations

from app.retrieval.product.filters.product_filter import ProductFilter
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)


async def retrieve(query_text: str, f: ProductFilter) -> list[dict]:
    """
    Vector similarity retrieval using OpenAI text-embedding-3-small.
    Returns [] on any failure — caller treats this as non-fatal fallback.
    """
    from app.repositories.vector_repository import vector_search

    rows = await vector_search(
        query_text=query_text,
        limit=f.limit,
        gender=f.gender,
        product_type=f.product_type,
        sport_type_hint=f.sport_type_hint,
        price_min=f.price_min,
        price_max=f.price_max,
        brand=f.brand,
        color=f.color,
    )
    logger.info(f"vector_retriever | hits={len(rows)} query={query_text[:40]!r}")
    return rows
