from __future__ import annotations


def enrich(rows: list[dict]) -> list[dict]:
    """
    Clean up raw DB rows from product_repository:
    - Ensure list fields are never None
    - Cast numeric types
    - Normalize None string fields
    """
    enriched = []
    for row in rows:
        enriched.append({
            "product_id":       row["product_id"],
            "name":             row["name"],
            "slug":             row["slug"],
            "short_description": row.get("short_description") or "",
            "gender":           row.get("gender"),
            "sport_type":       row.get("sport_type") or None,
            "product_type":     row.get("product_type") or None,
            "category_name":    row["category_name"],
            "brand_name":       row["brand_name"],
            "price_min":        float(row["price_min"] or 0),
            "price_max":        float(row["price_max"] or 0),
            "available_colors": _clean_list(row.get("available_colors")),
            "available_sizes":  _clean_list(row.get("available_sizes")),
            "total_available":  int(row.get("total_available") or 0),
            "primary_image":    row.get("primary_image"),
        })
    return enriched


def _clean_list(val) -> list[str]:
    """PostgreSQL array → clean Python list, removing None entries."""
    if not val:
        return []
    return [str(v) for v in val if v is not None]
