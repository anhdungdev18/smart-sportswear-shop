from __future__ import annotations

from typing import Any
from app.tools.registry import ToolDefinition
from app.services import cart_action_service

DEFINITION = ToolDefinition(
    name="add_to_cart",
    description="Thêm sản phẩm vào giỏ hàng. Gọi backend API để thực hiện.",
    capability="cart_action",
    requires_auth=True,
    requires_confirmation=False,
    input_schema={"variant_id": "string", "quantity": "integer"},
)


async def run(args: dict[str, Any]) -> dict[str, Any]:
    variant_id   = args.get("variant_id")
    quantity     = int(args.get("quantity") or 1)
    access_token = args.get("access_token")
    result = await cart_action_service.add_to_cart(variant_id, quantity, access_token)
    return result.model_dump()
