from __future__ import annotations

from pydantic import BaseModel, Field, field_validator


class InternalSearchFilters(BaseModel):
    categoryId: str | None = None
    categorySlug: str | None = None
    brandId: str | None = None
    brandSlug: str | None = None
    gender: str | None = None
    sportType: str | None = None
    productType: str | None = None
    surface: str | None = None
    color: str | None = None
    size: str | None = None
    minPrice: float | None = Field(None, ge=0)
    maxPrice: float | None = Field(None, ge=0)
    discount: str | None = None
    inStockOnly: bool = True


class InternalSearchRequest(BaseModel):
    query: str = Field(min_length=1, max_length=300)
    page: int = Field(1, ge=1)
    limit: int = Field(20, ge=1, le=100)
    filters: InternalSearchFilters = Field(default_factory=InternalSearchFilters)

    @field_validator("query")
    @classmethod
    def validate_query(cls, value: str) -> str:
        value = value.strip()
        if not value or any(ord(ch) < 32 and ch not in "\t" for ch in value):
            raise ValueError("query contains invalid control characters")
        return value


class InternalSearchItem(BaseModel):
    productId: str
    keywordRank: int | None = None
    semanticRank: int | None = None
    semanticScore: float | None = None
    fusionScore: float
    matchedReasons: list[str] = Field(default_factory=list)


class ParsedQueryResponse(BaseModel):
    normalized: str
    semanticText: str
    category: str | None = None
    brand: str | None = None
    gender: str | None = None
    sportType: str | None = None
    productType: str | None = None
    surface: str | None = None
    colorFamily: str | None = None
    size: str | None = None
    minPrice: float | None = None
    maxPrice: float | None = None
    featureHints: list[str] = Field(default_factory=list)


class InternalSearchResponse(BaseModel):
    items: list[InternalSearchItem]
    total: int
    parsedQuery: ParsedQueryResponse
    searchMode: str
    processingTimeMs: int
