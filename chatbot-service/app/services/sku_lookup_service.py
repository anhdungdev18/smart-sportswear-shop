from __future__ import annotations

from app.repositories import product_repository
from app.schemas.product import SkuLookupItem, SkuLookupResult


def _to_item(row: dict) -> SkuLookupItem:
    return SkuLookupItem(
        productId=row["product_id"],
        variantId=row["variant_id"],
        name=row["name"],
        slug=row["slug"],
        sku=row["sku"],
        category=row["category_name"],
        brandName=row["brand_name"],
        color=row.get("color"),
        size=row.get("size"),
        price=float(row["price"]),
        available=int(row.get("available") or 0),
        variantStatus=row["variant_status"],
        primaryImage=row.get("primary_image"),
    )


async def lookup(sku: str, limit: int = 10) -> SkuLookupResult:
    normalized = sku.strip().upper()
    if not normalized:
        return SkuLookupResult(items=[], total=0, querySku="", matchType="none")

    rows = await product_repository.find_variants_by_sku(normalized, exact=True, limit=1)
    match_type = "exact"
    if not rows:
        rows = await product_repository.find_variants_by_sku(normalized, exact=False, limit=limit)
        match_type = "partial" if rows else "none"

    items = [_to_item(row) for row in rows]
    return SkuLookupResult(
        items=items,
        total=len(items),
        querySku=normalized,
        matchType=match_type,
    )
