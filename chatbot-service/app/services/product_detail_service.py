from __future__ import annotations

from app.schemas.product import ProductDetailResult, VariantAvailabilityItem
from app.repositories import product_repository
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)


async def get_detail(
    product_id: str,
    size_hint: str | None = None,
    color_hint: str | None = None,
) -> ProductDetailResult:
    """
    Fetch full product detail + all variants.
    Returns ProductDetailResult with found=False when product_id is empty or not found.
    """
    if not product_id:
        # Caller could not resolve a product reference from context
        return ProductDetailResult(found=False)

    product = await product_repository.get_product_by_id(product_id)
    if not product:
        logger.info(f"product_detail_service | not_found product_id={product_id}")
        return ProductDetailResult(found=False, productId=product_id)

    variants_raw = await product_repository.get_product_variants(product_id)
    images = await product_repository.get_product_images(product_id)

    variants: list[VariantAvailabilityItem] = []
    for r in variants_raw:
        avail = max(int(r.get("stock_quantity", 0)) - int(r.get("reserved_quantity", 0)), 0)
        variants.append(VariantAvailabilityItem(
            variantId=str(r["variant_id"]),
            color=r.get("color"),
            size=r.get("size"),
            price=float(r["price"]),
            available=avail,
            sku=r.get("sku"),
        ))

    prices = [v.price for v in variants]
    price_min = min(prices) if prices else None
    price_max = max(prices) if prices else None

    logger.info(
        f"product_detail_service | found product_id={product_id} "
        f"variants={len(variants)} size_hint={size_hint} color_hint={color_hint}"
    )

    return ProductDetailResult(
        found=True,
        productId=str(product["id"]),
        name=product["name"],
        slug=product.get("slug"),
        category=product.get("category_name"),
        brandName=product.get("brand_name"),
        sportType=product.get("sport_type"),
        gender=product.get("gender"),
        description=product.get("short_description"),
        priceMin=price_min,
        priceMax=price_max,
        variants=variants,
        images=images,
        sizeHint=size_hint,
        colorHint=color_hint,
    )
