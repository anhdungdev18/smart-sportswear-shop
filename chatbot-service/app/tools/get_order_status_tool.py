from __future__ import annotations

from typing import Any
from app.tools.registry import ToolDefinition
from app.services import order_action_service

DEFINITION = ToolDefinition(
    name="get_order_status",
    description="Tra cứu trạng thái đơn hàng theo mã đơn.",
    capability="order_status",
    requires_auth=True,
    requires_confirmation=False,
    input_schema={"order_code": "string"},
)


async def run(args: dict[str, Any]) -> dict[str, Any]:
    order_code   = args.get("order_code") or ""
    access_token = args.get("access_token")
    result = await order_action_service.get_order_status(order_code, access_token)
    return result.model_dump()
