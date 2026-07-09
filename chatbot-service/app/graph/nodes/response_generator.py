from __future__ import annotations

from typing import Any
from app.graph.state import AgentState
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)


def _product_reply(result: dict[str, Any]) -> str:
    items = result.get("items", [])
    if not items:
        return "Tôi không tìm thấy sản phẩm phù hợp. Bạn có thể mô tả rõ hơn không?"
    names = ", ".join(i["name"] for i in items[:3])
    return f"Tôi tìm thấy {len(items)} sản phẩm phù hợp: {names}."


def _order_reply(result: dict[str, Any]) -> str:
    code = result.get("orderCode", "")
    status = result.get("status", "")
    return f"Đơn hàng {code} hiện đang ở trạng thái: {status}."


# Template generators per intent
_REPLY_BUILDERS: dict[str, Any] = {
    "PRODUCT_SEARCH": lambda r: _product_reply(r),
    "KNOWLEDGE_QA":   lambda r: f"Đây là thông tin tôi tìm được: {r.get('answer', '')}",
    "ORDER_STATUS":   lambda r: _order_reply(r),
    "ADD_TO_CART":    lambda r: "Tôi đã chuẩn bị yêu cầu thêm vào giỏ ở chế độ mock.",
    "CANCEL_ORDER":   lambda r: "Tôi đã chuẩn bị yêu cầu hủy đơn ở chế độ mock.",
    "UNKNOWN":        lambda r: "Tôi chưa hiểu rõ yêu cầu. Bạn có thể nói cụ thể hơn không?",
}


async def response_generator_node(state: AgentState) -> dict:
    intent = state["intent"] or "UNKNOWN"
    result = state["tool_result"] or {}
    builder = _REPLY_BUILDERS.get(intent, _REPLY_BUILDERS["UNKNOWN"])
    reply = builder(result)
    logger.info(f"[{state['session_id']}] response_generated | intent={intent} len={len(reply)}")
    return {"reply": reply}
