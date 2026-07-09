from __future__ import annotations

from typing import Any
from app.tools.registry import ToolDefinition

DEFINITION = ToolDefinition(
    name="add_to_cart",
    description="Thêm sản phẩm vào giỏ hàng. Gọi backend API để thực hiện.",
    capability="cart_action",
    requires_auth=True,
    requires_confirmation=False,
    input_schema={"variant_id": "string", "quantity": "integer"},
)


async def run(args: dict[str, Any]) -> dict[str, Any]:
    # TODO Phase 5: replace with CartApiClient -> Spring Boot backend
    return {
        "mock": True,
        "message": "Phase 1 mock — cart action not wired to backend yet.",
    }
