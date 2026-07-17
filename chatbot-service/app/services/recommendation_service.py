from __future__ import annotations

from app.schemas.product import RecommendedProductItem, RecommendationResult
from app.repositories import product_repository
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_RANK_REASON: dict[int, str] = {
    1: "Cùng danh mục sản phẩm",
    2: "Cùng môn thể thao",
    3: "Cùng thương hiệu",
}


async def recommend_related(product_id: str, limit: int = 4) -> RecommendationResult:
    """Mode A: find products related to a specific product from session context."""
    if not product_id:
        return RecommendationResult(
            items=[],
            basedOnProductId=None,
            mode="related",
            error=(
                "Bạn chưa chọn sản phẩm nào. "
                "Vui lòng tìm và xem một sản phẩm trước để tôi gợi ý mẫu tương tự."
            ),
        )

    rows = await product_repository.find_related_products(product_id, limit=limit)
    items = [
        RecommendedProductItem(
            productId=r["product_id"],
            name=r["name"],
            slug=r["slug"],
            categoryName=r.get("category_name"),
            sportType=r.get("sport_type"),
            priceMin=float(r["price_min"]) if r.get("price_min") is not None else None,
            primaryImage=r.get("primary_image"),
            reason=_RANK_REASON.get(r.get("relevance_rank", 3), "Sản phẩm liên quan"),
        )
        for r in rows
    ]
    logger.info(f"recommendation_service | mode=related product_id={product_id} found={len(items)}")
    return RecommendationResult(items=items, basedOnProductId=product_id, mode="related")


async def recommend_by_need(query: str, limit: int = 4) -> RecommendationResult:
    """Mode B: need-based — reuse the existing product search pipeline."""
    from app.services.product_search_service import search

    result = await search(query, limit=limit)
    items = [
        RecommendedProductItem(
            productId=it.productId,
            name=it.name,
            slug=it.slug,
            categoryName=it.category,
            sportType=it.sportType,
            priceMin=it.priceMin,
            primaryImage=it.primaryImage,
            reason="Phù hợp theo nhu cầu",
        )
        for it in result.items
    ]
    logger.info(f"recommendation_service | mode=need_based query={query!r} found={len(items)}")
    return RecommendationResult(items=items, basedOnProductId=None, mode="need_based")
