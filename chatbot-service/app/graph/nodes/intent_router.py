from __future__ import annotations

from app.graph.state import AgentState
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

# Rule-based keyword map — Phase 2+ can replace with LLM classifier
_INTENT_RULES: list[tuple[str, list[str]]] = [
    ("PRODUCT_SEARCH", [
        "tìm", "cần", "muốn mua", "mua", "giày", "áo", "quần", "phụ kiện",
        "sản phẩm", "shop có", "bán không", "có không", "có bán", "mẫu nào",
        "giá bao nhiêu", "bao nhiêu tiền", "rẻ", "đồ tập", "bộ thể thao",
    ]),
    ("KNOWLEDGE_QA", [
        "chính sách", "đổi trả", "giao hàng", "ship", "size", "bảo quản",
        "faq", "hướng dẫn", "quy định", "điều khoản", "bao lâu", "phí ship",
    ]),
    ("ORDER_STATUS", [
        "đơn hàng", "mã đơn", "trạng thái đơn", "tra cứu", "đơn của tôi",
        "đơn đâu rồi", "giao chưa", "đang giao",
    ]),
    ("ADD_TO_CART", [
        "thêm vào giỏ", "giỏ hàng", "thêm giỏ", "cho vào giỏ",
    ]),
    ("CANCEL_ORDER", [
        "hủy đơn", "huỷ đơn", "cancel đơn", "không muốn nữa",
    ]),
]


def detect_intent(message: str) -> str:
    lower = message.lower()
    for intent, keywords in _INTENT_RULES:
        if any(kw in lower for kw in keywords):
            return intent
    return "UNKNOWN"


async def intent_router_node(state: AgentState) -> dict:
    intent = detect_intent(state["message"])
    logger.info(f"[{state['session_id']}] intent_detected | intent={intent}")
    return {"intent": intent}
