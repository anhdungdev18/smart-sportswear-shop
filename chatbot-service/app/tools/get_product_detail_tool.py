from __future__ import annotations

from app.tools.registry import ToolDefinition
from app.services.product_detail_service import get_detail

DEFINITION = ToolDefinition(
    name="get_product_detail",
    description="Lấy chi tiết sản phẩm và thông tin variant (size, màu, tồn kho) từ DB",
    capability="product_search",
    requires_auth=False,
    requires_confirmation=False,
    input_schema={
        "product_id": {"type": "string", "description": "UUID của sản phẩm"},
        "size_hint": {"type": "string", "description": "Size cần kiểm tra (S/M/L/XL/39...)"},
        "color_hint": {"type": "string", "description": "Màu cần kiểm tra (đen, trắng...)"},
    },
)


async def run(args: dict) -> dict:
    result = await get_detail(
        args.get("product_id") or "",
        args.get("size_hint"),
        args.get("color_hint"),
    )
    return result.model_dump()
