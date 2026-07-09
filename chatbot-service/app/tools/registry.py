from __future__ import annotations

from typing import Any, Callable, Awaitable
from pydantic import BaseModel


class ToolDefinition(BaseModel):
    name: str
    description: str
    capability: str
    requires_auth: bool = False
    requires_confirmation: bool = False
    input_schema: dict[str, Any] = {}


class ToolRegistry:
    def __init__(self) -> None:
        self._tools: dict[str, tuple[ToolDefinition, Callable[..., Awaitable[dict[str, Any]]]]] = {}

    def register(self, definition: ToolDefinition, fn: Callable) -> None:
        self._tools[definition.name] = (definition, fn)

    def get(self, name: str) -> tuple[ToolDefinition, Callable] | None:
        return self._tools.get(name)

    def all_definitions(self) -> list[ToolDefinition]:
        return [defn for defn, _ in self._tools.values()]

    def all_metadata(self) -> list[dict[str, Any]]:
        return [defn.model_dump() for defn, _ in self._tools.values()]

    def names(self) -> list[str]:
        return list(self._tools.keys())


registry = ToolRegistry()


def setup_registry() -> None:
    """Register all tools. Called once at app startup in lifespan."""
    from app.tools.search_products_tool import DEFINITION as d1, run as r1
    from app.tools.answer_knowledge_tool import DEFINITION as d2, run as r2
    from app.tools.get_order_status_tool import DEFINITION as d3, run as r3
    from app.tools.add_to_cart_tool import DEFINITION as d4, run as r4
    from app.tools.cancel_order_tool import DEFINITION as d5, run as r5

    registry.register(d1, r1)
    registry.register(d2, r2)
    registry.register(d3, r3)
    registry.register(d4, r4)
    registry.register(d5, r5)
