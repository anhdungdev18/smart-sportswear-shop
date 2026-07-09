from __future__ import annotations

from typing import Any
from app.graph.state import AgentState
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_INTENT_TO_TOOL: dict[str, str] = {
    "PRODUCT_SEARCH": "search_products",
    "KNOWLEDGE_QA":   "answer_knowledge",
    "ORDER_STATUS":   "get_order_status",
    "ADD_TO_CART":    "add_to_cart",
    "CANCEL_ORDER":   "cancel_order",
    "UNKNOWN":        "none",
}


def _build_tool_args(intent: str, state: AgentState) -> dict[str, Any]:
    if intent in ("PRODUCT_SEARCH", "KNOWLEDGE_QA"):
        return {"query": state["message"]}
    if intent == "ORDER_STATUS":
        # TODO Phase 3: extract order_code from message via NER/regex
        return {"order_code": "mock-code"}
    return {}


async def tool_selector_node(state: AgentState) -> dict:
    intent = state["intent"]
    tool = _INTENT_TO_TOOL.get(intent, "none")
    args = _build_tool_args(intent, state)
    logger.info(f"[{state['session_id']}] tool_selected | tool={tool}")
    return {"selected_tool": tool, "tool_args": args}
