from typing import Any, Callable

# TODO Phase 1: implement full ToolRegistry with @register_tool decorator.
# Each tool entry must carry:
#   name, description, input_schema, requires_auth, requires_confirmation, capability_key


class ToolRegistry:
    """Manages available tools and their metadata. Phase 0 stub."""

    def __init__(self) -> None:
        self._tools: dict[str, dict[str, Any]] = {}

    def register(self, name: str, description: str, fn: Callable, **meta: Any) -> None:
        self._tools[name] = {"description": description, "fn": fn, **meta}

    def get_tools(self) -> list[dict[str, Any]]:
        return list(self._tools.values())

    def get_claude_schemas(self) -> list[dict[str, Any]]:
        # TODO Phase 1: export Anthropic-compatible tool schemas
        return []


registry = ToolRegistry()
