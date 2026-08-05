from __future__ import annotations

from app.graph.state import AgentState
from app.memory.base_store import is_pending_expired
from app.observability.trace_logger import get_logger
from app.retrieval.product.parser.sku_parser import extract_sku

logger = get_logger(__name__)

# Confirmation signals — only active when session has a pending_action
_CONFIRM_SIGNALS = frozenset({
    "đồng ý", "xác nhận", "ok", "oke", "yes",
    "hủy giúp tôi", "giúp tôi hủy", "đồng ý nha",
})
_REJECT_SIGNALS = frozenset({
    "không", "thôi", "hủy bỏ", "no", "bỏ qua",
    "không cần", "thôi đừng", "thôi không",
})

# Rule-based keyword map. First match wins — order matters.
_INTENT_RULES: list[tuple[str, list[str]]] = [
    ("CANCEL_ORDER", [
        "hủy đơn", "huỷ đơn", "cancel đơn", "không muốn nữa",
    ]),
    ("PRODUCT_DETAIL", [
        "mẫu này", "cái này", "sản phẩm này", "mẫu đó", "cái đó",
        "mẫu đầu tiên", "mẫu thứ", "cái thứ",
        "còn size", "còn màu",
        "size nào còn", "màu nào còn", "hết hàng chưa",
    ]),
    ("ADD_TO_CART", [
        "thêm vào giỏ", "giỏ hàng", "thêm giỏ", "cho vào giỏ", "vào giỏ",
        "bỏ vào giỏ", "đặt mua", "chốt đơn",
    ]),
    ("ORDER_STATUS", [
        "đơn hàng", "mã đơn", "trạng thái đơn", "tra cứu", "đơn của tôi",
        "đơn đâu rồi", "giao chưa", "đang giao",
    ]),
    ("SIZE_ADVISOR", [
        "chọn size", "mặc size nào", "đi size nào", "size bao nhiêu",
        "chiều cao", "cân nặng", "tôi cao", "tôi nặng",
        "bảng size", "hướng dẫn size", "size như thế nào", "gợi ý size",
    ]),
    ("RECOMMEND_PRODUCTS", [
        "gợi ý", "tương tự", "giống cái này", "cùng loại",
        "sản phẩm tương tự", "mẫu tương tự", "mẫu khác tương tự",
        "có gì phù hợp", "mẫu nào phù hợp", "recommend", "đề xuất",
        "outfit", "bộ đồ phù hợp",
    ]),
    ("KNOWLEDGE_QA", [
        "chính sách", "đổi trả", "giao hàng", "ship", "size", "bảo quản",
        "faq", "hướng dẫn", "quy định", "điều khoản", "bao lâu", "phí ship",
        "bảo hành", "thanh toán", "trả góp", "cod", "vnpay", "chuyển khoản",
        "hoàn tiền", "hóa đơn", "vận chuyển",
    ]),
    ("PRODUCT_SEARCH", [
        "tìm", "cần", "muốn mua", "mua", "giày", "áo", "quần", "phụ kiện",
        "sản phẩm", "shop có", "bán không", "có không", "có bán", "mẫu nào",
        "giá bao nhiêu", "bao nhiêu tiền", "rẻ", "đồ tập", "bộ thể thao",
        "có hàng", "còn hàng", "tất", "vớ", "balo", "mũ", "băng",
        # brands — a bare brand name is a product query ("có hàng Puma không")
        "nike", "adidas", "puma", "mizuno", "joma", "hummel", "under armour",
    ]),
]

# Base confidence per intent (single keyword match)
_INTENT_BASE_CONFIDENCE: dict[str, float] = {
    "SKU_LOOKUP":         0.99,
    "CANCEL_ORDER":       0.90,
    "PRODUCT_DETAIL":     0.82,
    "ADD_TO_CART":        0.92,
    "ORDER_STATUS":       0.88,
    "SIZE_ADVISOR":       0.85,
    "RECOMMEND_PRODUCTS": 0.80,
    "KNOWLEDGE_QA":       0.82,
    "PRODUCT_SEARCH":     0.76,  # broad — many keywords, moderate base
}


def detect_intent(message: str) -> tuple[str, float]:
    """Return (intent, confidence). More keyword matches → higher confidence."""
    if extract_sku(message):
        return "SKU_LOOKUP", _INTENT_BASE_CONFIDENCE["SKU_LOOKUP"]
    lower = message.lower()
    for intent, keywords in _INTENT_RULES:
        matched = [kw for kw in keywords if kw in lower]
        if matched:
            base = _INTENT_BASE_CONFIDENCE.get(intent, 0.76)
            # Each additional match adds 0.04, capped at 0.97
            confidence = min(base + 0.04 * (len(matched) - 1), 0.97)
            return intent, confidence
    return "UNKNOWN", 0.40


async def intent_router_node(state: AgentState) -> dict:
    # Phase 9: check pending confirmation BEFORE normal intent routing
    ctx = state.get("session_context") or {}
    pending = ctx.get("pending_action")

    if pending:
        lower = state["message"].lower().strip()
        created_at = ctx.get("pending_action_created_at")

        if not is_pending_expired(created_at):
            if any(s in lower for s in _CONFIRM_SIGNALS):
                logger.info(
                    f"[{state['session_id']}] intent_router | intent=CONFIRM_ACTION pending={pending}"
                )
                return {"intent": "CONFIRM_ACTION", "intent_confidence": 0.95}
            if any(s in lower for s in _REJECT_SIGNALS):
                logger.info(
                    f"[{state['session_id']}] intent_router | intent=REJECT_ACTION pending={pending}"
                )
                return {"intent": "REJECT_ACTION", "intent_confidence": 0.95}
        else:
            if any(s in lower for s in _CONFIRM_SIGNALS) or any(s in lower for s in _REJECT_SIGNALS):
                logger.info(
                    f"[{state['session_id']}] intent_router | intent=EXPIRED_CONFIRMATION pending={pending}"
                )
                return {"intent": "EXPIRED_CONFIRMATION", "intent_confidence": 0.95}

    intent, confidence = detect_intent(state["message"])
    logger.info(
        f"[{state['session_id']}] intent_router | intent={intent} confidence={confidence:.2f}"
    )
    return {"intent": intent, "intent_confidence": confidence}
