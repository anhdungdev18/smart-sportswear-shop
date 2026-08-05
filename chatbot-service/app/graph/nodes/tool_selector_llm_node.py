"""
LLM tool selector node — workflow path only (intent=UNKNOWN).

Calls tool_selector_llm.select_tool_for_unknown() to have the LLM pick
the best informational tool from the available set. Falls back to
selected_tool="none" on any failure.
"""
from __future__ import annotations

from app.graph.state import AgentState
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_TOOL_TO_INTENT = {
    "search_products": "PRODUCT_SEARCH",
    "get_product_detail": "PRODUCT_DETAIL",
    "recommend_products": "RECOMMEND_PRODUCTS",
    "size_advisor": "SIZE_ADVISOR",
    "answer_knowledge": "KNOWLEDGE_QA",
    "get_order_status": "ORDER_STATUS",
}


async def tool_selector_llm_node(state: AgentState) -> dict:
    from app.services.tool_selector_llm import select_tool_for_unknown

    llm_tool, llm_args = await select_tool_for_unknown(
        message=state["message"],
        session_context=state.get("session_context") or {},
        access_token=state.get("access_token"),
    )
    logger.info(
        f"[{state['session_id']}] tool_selector_llm_node | "
        f"tool={llm_tool} args={list(llm_args.keys())}"
    )
    out = {"selected_tool": llm_tool, "tool_args": llm_args}
    resolved_intent = _TOOL_TO_INTENT.get(llm_tool)
    if resolved_intent:
        # Downstream formatting, validation, and memory all dispatch by intent.
        out.update({"intent": resolved_intent, "intent_confidence": 0.90})
    return out
