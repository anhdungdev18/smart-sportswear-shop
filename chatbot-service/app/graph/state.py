from __future__ import annotations

from typing import Any, TypedDict


class AgentState(TypedDict):
    session_id: str
    user_id: str | None
    channel: str
    message: str
    intent: str         # PRODUCT_SEARCH | KNOWLEDGE_QA | ORDER_STATUS | ADD_TO_CART | CANCEL_ORDER | UNKNOWN
    selected_tool: str  # tool name from registry, or "none"
    tool_args: dict[str, Any]
    tool_result: dict[str, Any] | None
    reply: str
    errors: list[str]
