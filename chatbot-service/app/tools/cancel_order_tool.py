from __future__ import annotations

from typing import Any
from app.tools.registry import ToolDefinition
from app.services import order_action_service

DEFINITION = ToolDefinition(
    name="cancel_order",
    description="Hủy đơn hàng. Gọi backend API để thực hiện, cần xác nhận của user.",
    capability="order_action",
    requires_auth=True,
    requires_confirmation=True,
    input_schema={"order_code": "string", "reason": "string"},
)


async def run(args: dict[str, Any]) -> dict[str, Any]:
    order_code   = args.get("order_code") or ""
    reason       = args.get("reason")
    access_token = args.get("access_token")
    result = await order_action_service.cancel_order(order_code, access_token, reason)
    return result.model_dump()
