from __future__ import annotations

from typing import Any

from app.services import sku_lookup_service
from app.tools.registry import ToolDefinition

DEFINITION = ToolDefinition(
    name="lookup_product_by_sku",
    description="Tra cứu chính xác biến thể sản phẩm theo SKU hoặc một phần SKU.",
    capability="product_search",
    requires_auth=False,
    requires_confirmation=False,
    input_schema={"sku": "string"},
)


async def run(args: dict[str, Any]) -> dict[str, Any]:
    result = await sku_lookup_service.lookup(args.get("sku", ""))
    return result.model_dump()
