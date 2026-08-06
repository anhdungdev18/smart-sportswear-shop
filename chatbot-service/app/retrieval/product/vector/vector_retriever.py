from __future__ import annotations

from app.config.settings import settings
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
        category=f.category,
        color_family=f.color_family,
        surface=f.surface,
        size=f.size,
    )
    min_similarity = settings.PRODUCT_SEARCH_MIN_SIMILARITY
    filtered = [
        row for row in rows
        if float(row.get("vector_score") or 0.0) >= min_similarity
    ]
    logger.info(
        f"vector_retriever | hits={len(filtered)}/{len(rows)} "
        f"min_similarity={min_similarity:.2f} query={query_text[:40]!r}"
    )
    return filtered
