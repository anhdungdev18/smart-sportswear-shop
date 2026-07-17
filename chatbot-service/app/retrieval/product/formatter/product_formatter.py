from __future__ import annotations

from app.schemas.product import ProductSearchItem, ProductSearchResult, AppliedFilters
from app.retrieval.product.filters.product_filter import ProductFilter


def format_results(items: list[dict], f: ProductFilter) -> ProductSearchResult:
    schema_items = [_to_item(i) for i in items]
    return ProductSearchResult(
        items=schema_items,
        total=len(schema_items),
        appliedFilters=AppliedFilters(
            keyword=f.keyword,
            productType=f.product_type,
            sportType=f.sport_type_hint,
            gender=f.gender,
            priceMin=f.price_min,
            priceMax=f.price_max,
        ),
    )


def _to_item(row: dict) -> ProductSearchItem:
    return ProductSearchItem(
        productId=row["product_id"],
        name=row["name"],
        slug=row["slug"],
        category=row["category_name"],
        brandName=row["brand_name"],
        sportType=row.get("sport_type"),
        gender=row.get("gender"),
        priceMin=row["price_min"],
        priceMax=row["price_max"],
        availableColors=row["available_colors"],
        availableSizes=row["available_sizes"],
        totalAvailable=row["total_available"],
        primaryImage=row.get("primary_image"),
    )
