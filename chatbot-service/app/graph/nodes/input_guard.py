"""
Input guard node — first node in the graph.

Blocks empty / whitespace-only messages immediately so the rest of the
pipeline never runs. Sets execution_blocked=True and a friendly reply;
the routing function then sends the state straight to END.
"""
from __future__ import annotations

from app.graph.state import AgentState
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_EMPTY_REPLY = (
    "Bạn muốn hỏi gì vậy? Tôi có thể giúp bạn tìm sản phẩm, "
    "kiểm tra đơn hàng, tư vấn size, hoặc hỏi về chính sách shop."
)


async def input_guard_node(state: AgentState) -> dict:
    message = (state.get("message") or "").strip()
    if not message:
        logger.info(f"[{state['session_id']}] input_guard | blocked=empty_message")
        return {
            "execution_blocked": True,
            "intent": "",
            "reply": _EMPTY_REPLY,
        }
    return {}
