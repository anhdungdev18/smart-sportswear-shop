from __future__ import annotations

import hashlib
import json
from collections.abc import Mapping


def canonical_document(row: Mapping) -> str:
    attributes = row["attributes"] or {}
    if isinstance(attributes, str):
        attributes = json.loads(attributes)
    allowed = ("material", "fit", "weather", "features", "surface", "position", "team", "technology")
    searchable_attributes = " ".join(
        f"{key}: {attributes[key]}" for key in allowed if attributes.get(key)
    )
    fields = (
        ("Tên", row["name"]),
        ("SKU", " ".join(row["skus"])),
        ("Danh mục", row["category_name"]),
        ("Thương hiệu", row["brand_name"]),
        ("Loại sản phẩm", row["product_type"] or ""),
        ("Môn thể thao", row["sport_type"] or ""),
        ("Giới tính", row["gender"] or ""),
        ("Mặt sân", attributes.get("surface", "")),
        ("Màu", " ".join(row["colors"])),
        ("Size", " ".join(row["sizes"])),
        ("Mô tả", f"{row['short_description'] or ''} {row['description'] or ''}".strip()),
        ("Thuộc tính", searchable_attributes),
    )
    return "\n".join(f"{label}: {value}" for label, value in fields if str(value).strip())


def content_hash(document: str) -> str:
    return hashlib.sha256(document.encode("utf-8")).hexdigest()


PRODUCT_DOCUMENT_SQL = """
select p.id, p.name, p.short_description, p.description, p.gender,
       p.sport_type, p.product_type, p.attributes, p.status,
       c.name category_name, b.name brand_name,
       coalesce(array_agg(distinct pv.sku) filter (where pv.status='ACTIVE'), '{}') skus,
       coalesce(array_agg(distinct pv.color) filter (where pv.status='ACTIVE'), '{}') colors,
       coalesce(array_agg(distinct pv.size) filter (where pv.status='ACTIVE'), '{}') sizes
from products p
join categories c on c.id=p.category_id
join brands b on b.id=p.brand_id
left join product_variants pv on pv.product_id=p.id
where p.id=$1
group by p.id,c.name,b.name
"""
