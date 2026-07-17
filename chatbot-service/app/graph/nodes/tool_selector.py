from __future__ import annotations

import re
from typing import Any
from app.graph.state import AgentState
from app.memory import context_resolver
from app.observability.trace_logger import get_logger
from app.retrieval.product.parser.sku_parser import extract_sku

logger = get_logger(__name__)

_INTENT_TO_TOOL: dict[str, str] = {
    "SKU_LOOKUP":        "lookup_product_by_sku",
    "PRODUCT_SEARCH":    "search_products",
    "PRODUCT_DETAIL":    "get_product_detail",
    "RECOMMEND_PRODUCTS": "recommend_products",
    "SIZE_ADVISOR":      "size_advisor",
    "KNOWLEDGE_QA":      "answer_knowledge",
    "ORDER_STATUS":      "get_order_status",
    "ADD_TO_CART":       "add_to_cart",
    "CANCEL_ORDER":      "cancel_order",
    "UNKNOWN":           "none",
}

# Match order codes like DH001, DH1234, DH-001 (case-insensitive)
_ORDER_CODE_RE = re.compile(r"\b(DH[-]?\d+)\b", re.IGNORECASE)


def _extract_order_code(message: str) -> str | None:
    m = _ORDER_CODE_RE.search(message)
    return m.group(1).upper() if m else None


# Compound-query detection: run a second READ-only tool in parallel when the
# message clearly asks for both a product result and a policy/FAQ answer.
_KNOWLEDGE_KW = frozenset({
    "chính sách", "đổi trả", "đổi hàng", "trả hàng", "hoàn tiền", "giao hàng",
    "vận chuyển", "ship", "phí ship", "bảo hành", "bảo quản", "thanh toán",
    "bao lâu", "khuyến mãi", "quy định", "điều khoản",
})
_PRODUCT_NOUNS = frozenset({
    "áo", "quần", "giày", "dép", "phụ kiện", "balo", "túi", "tất", "vớ", "bóng", "vợt",
})
_PRODUCT_TOOLS = frozenset({
    "search_products", "get_product_detail", "recommend_products", "size_advisor",
})


def _detect_secondary_tools(primary_tool: str, state: AgentState) -> list[dict]:
    """Whitelist-only: pair a product tool with knowledge (or vice-versa) for compound queries."""
    low = state["message"].lower()
    has_knowledge = any(k in low for k in _KNOWLEDGE_KW)

    if primary_tool in _PRODUCT_TOOLS and has_knowledge:
        return [{"tool": "answer_knowledge", "args": {"query": state["message"]}}]

    if primary_tool == "answer_knowledge":
        pq = state.get("parsed_query") or {}
        has_product = bool(pq.get("product_type")) or any(n in low for n in _PRODUCT_NOUNS)
        if has_product:
            args: dict[str, Any] = {"query": state["message"]}
            if state.get("parsed_query"):
                args["parsed_query"] = state["parsed_query"]
            return [{"tool": "search_products", "args": args}]

    return []


def _build_tool_args(intent: str, state: AgentState) -> dict[str, Any]:
    message      = state["message"]
    access_token = state.get("access_token")

    if intent in ("PRODUCT_SEARCH", "KNOWLEDGE_QA"):
        args = {"query": message}
        if intent == "PRODUCT_SEARCH" and state.get("parsed_query"):
            args["parsed_query"] = state["parsed_query"]
        return args

    if intent == "SKU_LOOKUP":
        return {"sku": extract_sku(message) or ""}

    if intent == "PRODUCT_DETAIL":
        ctx = state.get("session_context") or {}
        product_id = context_resolver.resolve_product_reference(message, ctx) or ""
        hints = context_resolver.parse_variant_hints(message)
        logger.info(
            f"[{state['session_id']}] product_detail_resolve | "
            f"product_id={product_id!r} size={hints.get('size')} color={hints.get('color')}"
        )
        return {
            "product_id": product_id,
            "size_hint":  hints.get("size"),
            "color_hint": hints.get("color"),
        }

    if intent == "RECOMMEND_PRODUCTS":
        ctx = state.get("session_context") or {}
        selected_id = ctx.get("selected_product_id") or ""
        mode = "related" if selected_id else "need_based"
        logger.info(
            f"[{state['session_id']}] recommend_mode={mode} product_id={selected_id!r}"
        )
        return {"mode": mode, "product_id": selected_id, "query": message}

    if intent == "SIZE_ADVISOR":
        ctx = state.get("session_context") or {}
        product_id = context_resolver.resolve_product_reference(message, ctx)
        logger.info(
            f"[{state['session_id']}] size_advisor | "
            f"product_id={product_id!r} used_context={product_id is not None}"
        )
        return {"message": message, "product_id": product_id}

    if intent == "ORDER_STATUS":
        return {
            "order_code":   _extract_order_code(message) or "",
            "access_token": access_token,
        }

    if intent == "ADD_TO_CART":
        return {
            "variant_id":   None,
            "quantity":     1,
            "query":        message,
            "access_token": access_token,
        }

    if intent == "CANCEL_ORDER":
        return {
            "order_code":   _extract_order_code(message) or "",
            "reason":       None,
            "access_token": access_token,
        }

    return {}


async def tool_selector_node(state: AgentState) -> dict:
    intent = state["intent"]

    # Phase 9: rehydrate pending action from session when user confirmed
    if intent == "CONFIRM_ACTION":
        ctx = state.get("session_context") or {}
        payload = ctx.get("pending_action_payload") or {}
        tool_name = payload.get("tool", "none")
        # access_token is never stored in session — inject from current turn
        args = {**payload.get("args", {}), "access_token": state.get("access_token")}
        logger.info(f"[{state['session_id']}] confirm_action | tool={tool_name}")
        return {"selected_tool": tool_name, "tool_args": args}

    if intent in ("REJECT_ACTION", "EXPIRED_CONFIRMATION"):
        return {"selected_tool": "none", "tool_args": {}}

    tool = _INTENT_TO_TOOL.get(intent, "none")
    args = _build_tool_args(intent, state)

    secondary = _detect_secondary_tools(tool, state) if tool != "none" else []
    if secondary:
        logger.info(
            f"[{state['session_id']}] tool_selected | tool={tool} intent={intent} "
            f"+secondary={[s['tool'] for s in secondary]}"
        )
    else:
        logger.info(f"[{state['session_id']}] tool_selected | tool={tool} intent={intent}")

    return {"selected_tool": tool, "tool_args": args, "secondary_tools": secondary}
