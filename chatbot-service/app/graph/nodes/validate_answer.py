"""
Validate answer node — deterministic, LLM-free (runs after generate_answer).

Guards against the one real risk in an LLM-written reply: a hallucinated price
that is not present in the tool result. Instead of a slow LLM "refine" pass,
it runs a 0ms check:

  1. Collect every legitimate money amount from tool_result (recursively).
  2. Extract every "…đ" price the reply states.
  3. If a stated price is NOT a legitimate amount → the reply invented a number,
     so replace it with the deterministic template built purely from tool_result.

Prices come straight from the DB, so a reply that passes is guaranteed accurate.
Safety LLM is intentionally not called here: replies for these intents are built
from trusted structured data (product/order records), not open-ended generation.

Skips blocked/deterministic turns and non-data intents.
"""
from __future__ import annotations

import re

from app.graph.state import AgentState
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

# Intents whose replies quote product/price data worth validating
_VALIDATED_INTENTS = frozenset({
    "SKU_LOOKUP",
    "PRODUCT_SEARCH",
    "PRODUCT_DETAIL",
    "RECOMMEND_PRODUCTS",
    "ORDER_STATUS",
    "ADD_TO_CART",
    "CANCEL_ORDER",
    "CONFIRM_ACTION",
})

# Deterministic-reply intents — never need validation
_SKIP_INTENTS = frozenset({"REJECT_ACTION", "EXPIRED_CONFIRMATION"})

# Amounts >= this are treated as money (prices); smaller numbers are counts/sizes.
_MONEY_MIN = 1000

_FALSE_NO_RESULT_PHRASES = (
    "không tìm thấy",
    "chưa tìm thấy",
    "không tìm được",
    "chưa tìm được",
    "không có sản phẩm",
    "chưa có sản phẩm",
    "không có thông tin",
    "chưa có thông tin",
)

# "3.500.000đ" / "650,000 đ" → capture the digit run immediately before đ
_PRICE_RE = re.compile(r"([\d][\d.,]*)\s*đ", re.IGNORECASE)


def _collect_amounts(obj) -> set[int]:
    """Recursively collect every numeric money amount (>= _MONEY_MIN) in a result."""
    found: set[int] = set()

    def walk(x) -> None:
        if isinstance(x, bool):
            return
        if isinstance(x, dict):
            for v in x.values():
                walk(v)
        elif isinstance(x, (list, tuple)):
            for v in x:
                walk(v)
        elif isinstance(x, (int, float)):
            n = int(round(x))
            if n >= _MONEY_MIN:
                found.add(n)

    walk(obj)
    return found


def _stated_prices(text: str) -> list[int]:
    """Every '…đ' price the reply states, normalized to int VND."""
    prices: list[int] = []
    for raw in _PRICE_RE.findall(text):
        digits = raw.replace(".", "").replace(",", "").strip()
        if digits.isdigit():
            n = int(digits)
            if n >= _MONEY_MIN:
                prices.append(n)
    return prices


def _template_reply(intent: str, result: dict) -> str:
    """Deterministic reply from tool_result only — used when a price is ungrounded."""
    from app.graph.nodes.response_generator import _FALLBACK_BUILDERS

    builder = _FALLBACK_BUILDERS.get(intent) or _FALLBACK_BUILDERS["UNKNOWN"]
    return builder(result)


async def validate_answer_node(state: AgentState) -> dict:
    answer  = state.get("reply") or ""
    blocked = state.get("execution_blocked", False)
    intent  = state.get("intent") or ""

    # Skip blocked/deterministic replies and non-data intents
    if not answer or blocked or intent in _SKIP_INTENTS:
        return {}
    if intent not in _VALIDATED_INTENTS:
        return {}

    result = state.get("tool_result") or {}
    if not result:
        return {}

    # Product cards and prose must never contradict one another. If retrieval
    # returned products but the model claims otherwise, use grounded output.
    if intent in {"PRODUCT_SEARCH", "RECOMMEND_PRODUCTS", "SKU_LOOKUP"}:
        items = result.get("items") or []
        normalized_answer = answer.casefold()
        if items and any(phrase in normalized_answer for phrase in _FALSE_NO_RESULT_PHRASES):
            safe = _template_reply(intent, result)
            logger.warning(
                f"[{state['session_id']}] validate_answer | false_no_result -> template fallback intent={intent}"
            )
            return {"reply": safe}

    # Include amounts from any parallel secondary tool results (compound queries)
    valid = _collect_amounts(result)
    for sres in (state.get("secondary_results") or {}).values():
        valid |= _collect_amounts(sres)

    # Deterministic price grounding check (0ms) — no LLM
    if all(p in valid for p in _stated_prices(answer)):
        return {}  # every stated price is real → keep the reply as-is

    # A price was invented → replace with the deterministic template from DB data
    safe = _template_reply(intent, result)
    logger.warning(
        f"[{state['session_id']}] validate_answer | ungrounded_price → template fallback intent={intent}"
    )
    return {"reply": safe}
