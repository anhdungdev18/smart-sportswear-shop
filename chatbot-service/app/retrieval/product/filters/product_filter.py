from __future__ import annotations

from dataclasses import dataclass, field
from app.retrieval.product.parser.query_parser import ParsedQuery


@dataclass
class ProductFilter:
    keyword: str | None = None
    product_type: str | None = None     # APPAREL | FOOTWEAR | ACCESSORY | EQUIPMENT
    sport_type_hint: str | None = None  # ILIKE on p.sport_type
    gender: str | None = None           # MEN | WOMEN | UNISEX
    brand: str | None = None            # exact brands.name
    color: str | None = None            # ILIKE token on pv.color
    price_min: float | None = None
    price_max: float | None = None
    feature_hints: list[str] = field(default_factory=list)
    limit: int = 5

    @classmethod
    def from_parsed(cls, parsed: ParsedQuery, limit: int = 5) -> ProductFilter:
        return cls(
            keyword=parsed.keyword or None,
            product_type=parsed.product_type,
            sport_type_hint=parsed.sport_type_hint,
            gender=parsed.gender,
            brand=parsed.brand,
            color=parsed.color,
            price_min=parsed.price_min,
            price_max=parsed.price_max,
            feature_hints=parsed.feature_hints,
            limit=limit,
        )

    def has_any_structural_filter(self) -> bool:
        return any([
            self.product_type,
            self.sport_type_hint,
            self.gender,
            self.brand,
            self.color,
            self.price_min,
            self.price_max,
        ])
