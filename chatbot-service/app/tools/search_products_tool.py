from __future__ import annotations

from typing import Any
from app.tools.registry import ToolDefinition

DEFINITION = ToolDefinition(
    name="search_products",
    description="Tìm sản phẩm theo từ khóa, danh mục, thể thao, giới tính, khoảng giá.",
    capability="product_search",
    requires_auth=False,
    requires_confirmation=False,
    input_schema={"query": "string"},
)


async def run(args: dict[str, Any]) -> dict[str, Any]:
    # TODO Phase 3: replace with ProductSearchService + ProductRepository
    return {
        "items": [
            {
                "productId": "mock-1",
                "name": "Giày chạy bộ Dry-Fit",
                "category": "Giày",
                "sportType": "Chạy bộ",
                "priceMin": 650000,
                "priceMax": 890000,
                "availableColors": ["Đen", "Trắng"],
                "availableSizes": ["39", "40", "41", "42"],
                "totalAvailable": 8,
            },
            {
                "productId": "mock-2",
                "name": "Áo thể thao thoáng khí",
                "category": "Áo",
                "sportType": "Đa năng",
                "priceMin": 280000,
                "priceMax": 390000,
                "availableColors": ["Xanh", "Đỏ", "Đen"],
                "availableSizes": ["S", "M", "L", "XL"],
                "totalAvailable": 20,
            },
        ],
        "total": 2,
        "query": args.get("query", ""),
        "_mock": True,
    }
