"""
select_mode node — chọn execution path dựa trên intent + confidence.

fast     → intent đã xác định rõ (confidence ≥ FAST_THRESHOLD)
workflow → confidence trung bình + có OPENAI_API_KEY; LLM chọn tool
clarify  → confidence quá thấp hoặc không có LLM key

Thresholds giống DigiAI (FAST=0.75, CLARIFY=0.55).
Intents đặc biệt (CONFIRM/REJECT/EXPIRED) luôn fast bất kể confidence.
"""
from __future__ import annotations

from app.graph.state import AgentState
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_FAST_THRESHOLD    = 0.75   # confidence ≥ này → fast path
_CLARIFY_THRESHOLD = 0.55   # confidence < này → clarify (hoặc workflow nếu có LLM)

# Intents luôn fast — không cần LLM tool selection
_ALWAYS_FAST = frozenset({
    "CONFIRM_ACTION",
    "REJECT_ACTION",
    "EXPIRED_CONFIRMATION",
})

# Intents có tool đã biết sẵn → fast khi confidence đủ cao
_KNOWN_INTENTS = frozenset({
    "SKU_LOOKUP",
    "PRODUCT_SEARCH",
    "PRODUCT_DETAIL",
    "RECOMMEND_PRODUCTS",
    "SIZE_ADVISOR",
    "KNOWLEDGE_QA",
    "ORDER_STATUS",
    "ADD_TO_CART",
    "CANCEL_ORDER",
})


async def select_mode_node(state: AgentState) -> dict:
    intent     = state.get("intent") or ""
    confidence = state.get("intent_confidence", 0.0)
    sid        = state["session_id"]

    # Special intents — luôn fast, không cần LLM
    if intent in _ALWAYS_FAST:
        logger.info(f"[{sid}] select_mode | mode=fast intent={intent} reason=always_fast")
        return {"execution_mode": "fast"}

    # Known intent + đủ confidence → fast (deterministic tool mapping)
    if intent in _KNOWN_INTENTS and confidence >= _FAST_THRESHOLD:
        logger.info(f"[{sid}] select_mode | mode=fast intent={intent} confidence={confidence:.2f}")
        return {"execution_mode": "fast"}

    # Check the configured provider instead of assuming OpenAI.
    from app.services.llm_client import is_available
    has_llm = is_available()

    # Confidence quá thấp (UNKNOWN hoặc rất mơ hồ) → workflow nếu có LLM, còn không clarify
    if confidence < _CLARIFY_THRESHOLD:
        if has_llm:
            logger.info(
                f"[{sid}] select_mode | mode=workflow intent={intent} confidence={confidence:.2f} reason=low_conf_llm"
            )
            return {"execution_mode": "workflow"}
        logger.info(
            f"[{sid}] select_mode | mode=clarify intent={intent} confidence={confidence:.2f} reason=low_conf_no_llm"
        )
        return {"execution_mode": "clarify"}

    # Confidence trung bình (0.55–0.74) → workflow nếu có LLM để enrich/chọn tool tốt hơn
    if has_llm:
        logger.info(
            f"[{sid}] select_mode | mode=workflow intent={intent} confidence={confidence:.2f} reason=medium_conf"
        )
        return {"execution_mode": "workflow"}

    logger.info(
        f"[{sid}] select_mode | mode=clarify intent={intent} confidence={confidence:.2f} reason=no_llm"
    )
    return {"execution_mode": "clarify"}
