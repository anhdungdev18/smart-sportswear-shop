from __future__ import annotations

from typing import Any
from app.tools.registry import ToolDefinition

DEFINITION = ToolDefinition(
    name="get_order_status",
    description="Tra cứu trạng thái đơn hàng theo mã đơn.",
    capability="order_status",
    requires_auth=False,
    requires_confirmation=False,
    input_schema={"order_code": "string"},
)


async def run(args: dict[str, Any]) -> dict[str, Any]:
    # TODO Phase 5: replace with OrderApiClient -> Spring Boot backend
    return {
        "orderCode": args.get("order_code", "DH001"),
        "status": "PENDING_CONFIRMATION",
        "updatedAt": "2026-07-09",
        "_mock": True,
    }
