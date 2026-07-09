from __future__ import annotations

from typing import Any
from app.tools.registry import ToolDefinition

DEFINITION = ToolDefinition(
    name="cancel_order",
    description="Hủy đơn hàng. Gọi backend API để thực hiện, cần xác nhận của user.",
    capability="order_action",
    requires_auth=True,
    requires_confirmation=True,
    input_schema={"order_code": "string", "reason": "string"},
)


async def run(args: dict[str, Any]) -> dict[str, Any]:
    # TODO Phase 5: replace with OrderApiClient -> Spring Boot backend
    return {
        "mock": True,
        "message": "Phase 1 mock — cancel action not wired to backend yet.",
    }
