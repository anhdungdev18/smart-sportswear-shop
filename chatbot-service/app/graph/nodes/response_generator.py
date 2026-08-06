"""
Response generator node — Phase 10: LLM writes the reply.

Flow:
  1. Blocked / transactional intents → deterministic template (no LLM, must be precise)
  2. All other intents → format tool result as [DỮ LIỆU] context, call LLM
  3. LLM failure (no key, timeout, error) → fall back to legacy template
"""
from __future__ import annotations

import os
from pathlib import Path
from typing import Any

from app.graph.state import AgentState
from app.graph.stream_context import stream_sink
from app.observability.trace_logger import get_logger
from app.services import llm_client

logger = get_logger(__name__)

_MAX_HISTORY = 10  # messages kept in LLM context (5 turns)

# ── System prompt ──────────────────────────────────────────────────────────────

def _load_system_prompt() -> str:
    path = Path(__file__).parent.parent / "system_prompt.txt"
    try:
        return path.read_text(encoding="utf-8").strip()
    except Exception:
        return (
            "Bạn là trợ lý tư vấn bán hàng thể thao. "
            "Trả lời bằng tiếng Việt, ngắn gọn, dựa trên dữ liệu được cung cấp."
        )


_SYSTEM_PROMPT = _load_system_prompt()


# ── Context formatters (tool result → text block for LLM) ─────────────────────

def _ctx_product_search(result: dict) -> str:
    items = result.get("items") or []
    total = result.get("total", 0)
    if not items:
        kw = (result.get("appliedFilters") or {}).get("keyword", "")
        return f"[Tìm kiếm] Không tìm thấy sản phẩm nào khớp với '{kw}'."
    lines = [f"[Kết quả tìm kiếm — {total} sản phẩm phù hợp, hiển thị {min(len(items), 5)} đầu]"]
    for i, item in enumerate(items[:5], 1):
        p_min = item.get("priceMin", 0)
        p_max = item.get("priceMax", 0)
        price = f"{p_min:,.0f}đ–{p_max:,.0f}đ" if p_max > p_min else f"{p_min:,.0f}đ"
        sizes  = ", ".join((item.get("availableSizes") or [])[:6]) or "đang cập nhật"
        colors = ", ".join((item.get("availableColors") or [])[:4]) or "đang cập nhật"
        stock  = item.get("totalAvailable", 0)
        lines.append(
            f"{i}. {item.get('name','')} | Giá: {price} | Size: {sizes} | Màu: {colors} | Còn: {stock} cái"
        )
    return "\n".join(lines)


def _ctx_sku_lookup(result: dict) -> str:
    items = result.get("items") or []
    sku = result.get("querySku", "")
    if not items:
        return f"[Tra cứu SKU] Không tìm thấy biến thể khớp với '{sku}'."
    lines = [f"[Tra cứu SKU — {result.get('matchType', 'none')}]" ]
    for item in items[:10]:
        lines.append(
            f"• {item.get('sku', '')} | {item.get('name', '')} | "
            f"Màu: {item.get('color') or 'N/A'} | Size: {item.get('size') or 'N/A'} | "
            f"Giá: {item.get('price', 0):,.0f}đ | Còn: {item.get('available', 0)} cái"
        )
    return "\n".join(lines)


def _ctx_product_detail(result: dict) -> str:
    if not result.get("found"):
        pid = result.get("productId")
        return "[Chi tiết] Không tìm thấy sản phẩm." if pid else "[Chi tiết] Chưa xác định được sản phẩm."

    name     = result.get("name", "")
    p_min    = result.get("priceMin")
    p_max    = result.get("priceMax")
    price    = (
        f"{p_min:,.0f}đ–{p_max:,.0f}đ" if p_max and p_min and p_max > p_min
        else f"{p_min:,.0f}đ" if p_min else "đang cập nhật"
    )
    variants = result.get("variants") or []
    avail    = [v for v in variants if v.get("available", 0) > 0]
    colors   = sorted({v["color"] for v in avail if v.get("color")})
    sizes    = sorted({v["size"]  for v in avail if v.get("size")})
    total    = sum(v.get("available", 0) for v in avail)

    lines = [
        f"[Chi tiết sản phẩm: {name}]",
        f"Giá: {price}",
        f"Màu khả dụng: {', '.join(colors[:6]) or 'đang cập nhật'}",
        f"Size khả dụng: {', '.join(sizes[:8]) or 'đang cập nhật'}",
        f"Tồn kho: {total} cái" if total > 0 else "Tồn kho: tạm hết",
    ]
    if result.get("sportType"):
        lines.append(f"Môn thể thao: {result['sportType']}")
    if result.get("sizeHint") or result.get("colorHint"):
        lines.append(f"Khách hỏi về: size={result.get('sizeHint')} màu={result.get('colorHint')}")
    return "\n".join(lines)


def _ctx_knowledge(result: dict) -> str:
    answers = result.get("answers") or []
    if not answers:
        return "[Chính sách/FAQ] Không tìm thấy thông tin phù hợp."
    best = answers[0]
    lines = [f"[Chính sách/FAQ: {best.get('title', '')}]", best.get("content", "")]
    if len(answers) > 1:
        extras = [a.get("title", "") for a in answers[1:]]
        lines.append(f"Thông tin liên quan: {', '.join(extras)}")
    return "\n".join(lines)


def _ctx_recommend(result: dict) -> str:
    items = result.get("items") or []
    if not items:
        return "[Gợi ý] Không tìm thấy sản phẩm phù hợp."
    mode   = result.get("mode", "")
    header = "[Gợi ý tương tự]" if mode == "related" else "[Gợi ý theo nhu cầu]"
    lines  = [header]
    for item in items[:5]:
        price  = f"{item['priceMin']:,.0f}đ" if item.get("priceMin") is not None else "liên hệ"
        reason = f" ({item['reason']})" if item.get("reason") else ""
        lines.append(f"• {item.get('name', '')} — từ {price}{reason}")
    return "\n".join(lines)


def _ctx_size_advisor(result: dict) -> str:
    source = result.get("source", "unknown")
    if result.get("error"):
        return f"[Tư vấn size] {result['error']}"
    if source == "product_variants":
        sizes = result.get("availableSizes") or []
        return (
            f"[Size sản phẩm: {result.get('productName', '')}]\n"
            f"Size khả dụng: {', '.join(sizes) if sizes else 'tạm hết tất cả size'}"
        )
    if source == "rule_based":
        suggested = result.get("suggestedSize")
        caveat    = result.get("caveat", "")
        return (
            f"[Gợi ý size theo số đo]\n"
            f"Size tham khảo: {suggested or 'chưa xác định'}\n"
            f"Lưu ý: {caveat}"
        )
    if source == "knowledge":
        return f"[Hướng dẫn size]\n{result.get('knowledgeTitle', '')}\n{result.get('knowledgeContent', '')}"
    return "[Tư vấn size] Không tìm thấy hướng dẫn phù hợp."


def _ctx_order_status(result: dict) -> str:
    if not result.get("success"):
        return f"[Đơn hàng] Lỗi: {result.get('error', 'không lấy được thông tin')}"
    total = result.get("totalAmount")
    return "\n".join(filter(None, [
        f"[Đơn hàng: {result.get('orderCode', '')}]",
        f"Trạng thái: {result.get('orderStatusLabel') or result.get('orderStatus', '')}",
        f"Thanh toán: {result.get('paymentStatusLabel') or result.get('paymentStatus', '')}",
        f"Tổng tiền: {total:,.0f}đ" if total is not None else None,
    ]))


def _ctx_add_to_cart(result: dict) -> str:
    if not result.get("success"):
        return f"[Giỏ hàng] Lỗi: {result.get('error', 'không thêm được')}"
    count    = result.get("itemCount")
    subtotal = result.get("subtotal")
    parts    = ["[Giỏ hàng] Đã thêm sản phẩm thành công."]
    if count is not None:
        parts.append(f"Giỏ hàng: {count} sản phẩm.")
    if subtotal is not None:
        parts.append(f"Tạm tính: {subtotal:,.0f}đ.")
    return " ".join(parts)


def _ctx_cancel_order(result: dict) -> str:
    if not result.get("success"):
        return f"[Hủy đơn] Lỗi: {result.get('error', 'không hủy được')}"
    return (
        f"[Hủy đơn thành công: {result.get('orderCode', '')}]\n"
        f"Trạng thái: {result.get('orderStatusLabel') or result.get('orderStatus', '')}"
    )


_CTX_FORMATTERS: dict[str, Any] = {
    "SKU_LOOKUP":         _ctx_sku_lookup,
    "PRODUCT_SEARCH":     _ctx_product_search,
    "PRODUCT_DETAIL":     _ctx_product_detail,
    "RECOMMEND_PRODUCTS": _ctx_recommend,
    "SIZE_ADVISOR":       _ctx_size_advisor,
    "KNOWLEDGE_QA":       _ctx_knowledge,
    "ORDER_STATUS":       _ctx_order_status,
    "ADD_TO_CART":        _ctx_add_to_cart,
    "CANCEL_ORDER":       _ctx_cancel_order,
    "CONFIRM_ACTION":     _ctx_cancel_order,  # used after confirmed cancel/cart
}

# Formatters for parallel secondary tools (keyed by tool name, not intent)
_SECONDARY_TOOL_CTX: dict[str, Any] = {
    "answer_knowledge": _ctx_knowledge,
    "search_products":  _ctx_product_search,
}


# ── Legacy template fallbacks (used when LLM unavailable) ─────────────────────

def _fallback_product_search(result: dict) -> str:
    items = result.get("items") or []
    total = result.get("total", 0)
    if not items:
        kw = (result.get("appliedFilters") or {}).get("keyword", "")
        return f"Tôi chưa tìm thấy sản phẩm phù hợp với '{kw}'. Bạn thử từ khóa khác không?"
    lines = [f"Tôi tìm thấy {total} sản phẩm:"]
    for item in items[:3]:
        p = item.get("priceMin", 0)
        lines.append(f"• {item.get('name','')} — từ {p:,.0f}đ | Còn: {item.get('totalAvailable',0)} cái")
    if total > 3:
        lines.append(f"...và {total-3} sản phẩm khác. Bạn muốn xem thêm không?")
    return "\n".join(lines)


def _fallback_sku_lookup(result: dict) -> str:
    items = result.get("items") or []
    sku = result.get("querySku", "")
    if not items:
        return f"Tôi không tìm thấy biến thể có SKU **{sku}**."
    if result.get("matchType") == "exact":
        item = items[0]
        return (
            f"SKU **{item['sku']}** là {item['name']}, màu {item.get('color') or 'N/A'}, "
            f"size {item.get('size') or 'N/A'}, giá {item.get('price', 0):,.0f}đ, "
            f"hiện còn {item.get('available', 0)} sản phẩm."
        )
    lines = [f"Tôi tìm thấy {len(items)} SKU gần với **{sku}**:"]
    lines.extend(
        f"• {item['sku']} — {item['name']} ({item.get('color') or 'N/A'}, size {item.get('size') or 'N/A'})"
        for item in items[:10]
    )
    return "\n".join(lines)


def _fallback_knowledge(result: dict) -> str:
    answers = result.get("answers") or []
    if not answers:
        return "Tôi chưa tìm thấy thông tin. Bạn có thể liên hệ shop trực tiếp."
    best = answers[0]
    return f"**{best.get('title','')}**\n{best.get('content','')}"


def _fallback_order_status(result: dict) -> str:
    if not result.get("success"):
        return result.get("error") or "Không lấy được thông tin đơn hàng."
    total = result.get("totalAmount")
    parts = [f"Đơn **{result.get('orderCode','')}**: {result.get('orderStatusLabel','')}.",
             f"Thanh toán: {result.get('paymentStatusLabel','')}."]
    if total is not None:
        parts.append(f"Tổng: {total:,.0f}đ.")
    return " ".join(parts)


def _fallback_cancel(result: dict) -> str:
    if not result.get("success"):
        return result.get("error") or "Không thể hủy đơn hàng."
    return f"Đã gửi yêu cầu hủy đơn **{result.get('orderCode','')}**. Trạng thái: {result.get('orderStatusLabel','')}."


def _fallback_add_to_cart(result: dict) -> str:
    if not result.get("success"):
        return result.get("error") or "Không thể thêm vào giỏ."
    return f"Đã thêm vào giỏ. Giỏ hàng: {result.get('itemCount','')} sản phẩm."


_FALLBACK_BUILDERS: dict[str, Any] = {
    "SKU_LOOKUP":         _fallback_sku_lookup,
    "PRODUCT_SEARCH":     _fallback_product_search,
    "PRODUCT_DETAIL":     lambda r: "Tôi chưa tìm thấy thông tin sản phẩm này.",
    "RECOMMEND_PRODUCTS": lambda r: "Tôi chưa tìm được gợi ý phù hợp.",
    "SIZE_ADVISOR":       lambda r: r.get("error") or "Tôi chưa xác định được size phù hợp.",
    "KNOWLEDGE_QA":       _fallback_knowledge,
    "ORDER_STATUS":       _fallback_order_status,
    "ADD_TO_CART":        _fallback_add_to_cart,
    "CANCEL_ORDER":       _fallback_cancel,
    "CONFIRM_ACTION":     _fallback_cancel,
    "UNKNOWN":            lambda r: "Tôi chưa hiểu rõ yêu cầu. Bạn có thể nói cụ thể hơn không?",
}

# Deterministic replies — never routed to LLM
_BLOCKED_REPLIES: dict[str, str] = {
    "capability_disabled": "Tính năng này hiện đang bị tắt. Vui lòng thử lại sau.",
    "auth_required":       "Bạn cần đăng nhập để sử dụng tính năng này.",
    "unknown_tool":        "Xin lỗi, tôi không thể xử lý yêu cầu này lúc này.",
}


# ── Main node ─────────────────────────────────────────────────────────────────

async def response_generator_node(state: AgentState) -> dict:
    sid    = state["session_id"]
    intent = state["intent"] or "UNKNOWN"
    result = state.get("tool_result") or {}

    # 1. Blocked by policy — deterministic, no LLM
    if state["execution_blocked"]:
        reason = state["policy_reason"]
        if reason == "confirmation_required":
            display = state.get("pending_action_display") or "thao tác này"
            reply = (
                f"Bạn có chắc muốn {display} không? "
                "Nhập **'đồng ý'** để tiếp tục hoặc **'không'** để hủy bỏ."
            )
        else:
            reply = _BLOCKED_REPLIES.get(reason, "Yêu cầu bị từ chối.")
        logger.info(f"[{sid}] response_generated | blocked reason={reason}")
        return {"reply": reply}

    # 2. Transactional confirmation intents — deterministic, no LLM
    if intent == "REJECT_ACTION":
        return {"reply": "Tôi đã hủy thao tác trước đó. Bạn cần giúp gì thêm không?"}

    if intent == "EXPIRED_CONFIRMATION":
        return {"reply": "Thao tác đã hết hạn xác nhận. Bạn hãy gửi lại yêu cầu nếu muốn tiếp tục."}

    # Empty retrieval results are deterministic. Letting the LLM elaborate here
    # can suggest product types that are not present in the current catalog.
    if intent in {"PRODUCT_SEARCH", "RECOMMEND_PRODUCTS", "SKU_LOOKUP"}:
        if result and not (result.get("items") or []):
            fallback = _FALLBACK_BUILDERS.get(intent, _FALLBACK_BUILDERS["UNKNOWN"])
            return {"reply": fallback(result)}

    # 3. LLM-generated reply
    reply = await _llm_reply(state, intent, result)
    logger.info(f"[{sid}] response_generated | intent={intent} len={len(reply)}")
    return {"reply": reply}


async def _llm_reply(state: AgentState, intent: str, result: dict) -> str:
    """Build LLM messages, call API, fall back to template on failure."""

    # Format tool result as context block
    fmt = _CTX_FORMATTERS.get(intent)
    context_block = fmt(result) if fmt and result else None

    # Append context from any parallel read-only secondary tools (compound queries)
    secondary_results = state.get("secondary_results") or {}
    extra_blocks = [
        _SECONDARY_TOOL_CTX[name](sres)
        for name, sres in secondary_results.items()
        if name in _SECONDARY_TOOL_CTX and sres
    ]
    if extra_blocks:
        context_block = "\n\n".join(filter(None, [context_block, *extra_blocks])).strip()

    # For CONFIRM_ACTION: choose the right context formatter based on the tool
    if intent == "CONFIRM_ACTION":
        tool = state.get("selected_tool", "none")
        if tool == "add_to_cart":
            context_block = _ctx_add_to_cart(result)
        elif tool == "cancel_order":
            context_block = _ctx_cancel_order(result)
        elif not result:
            return "Tôi không còn thấy thao tác chờ xác nhận. Bạn hãy gửi lại yêu cầu."

    # Build user message content
    if context_block:
        user_content = f"[DỮ LIỆU]\n{context_block}\n\n[CÂU HỎI]\n{state['message']}"
    else:
        # No tool data for this turn (e.g. UNKNOWN intent). Tell the model explicitly
        # so it greets / asks a clarifying question instead of inventing products.
        user_content = (
            "[DỮ LIỆU]\n(Không có dữ liệu sản phẩm hay chính sách cho câu hỏi này.)\n\n"
            "[HƯỚNG DẪN]\nTUYỆT ĐỐI không liệt kê hay bịa tên sản phẩm, giá, tồn kho. "
            "Hãy trả lời ngắn gọn và hỏi khách cần tìm sản phẩm gì cụ thể.\n\n"
            f"[CÂU HỎI]\n{state['message']}"
        )

    # Assemble messages: system + trimmed history + current turn
    history = (state.get("chat_history") or [])[-_MAX_HISTORY:]
    messages = [
        {"role": "system", "content": _SYSTEM_PROMPT},
        *history,
        {"role": "user", "content": user_content},
    ]

    # Streaming path: when a sink is set (SSE request), stream deltas into it and
    # accumulate the full text so validate/save downstream still see a real reply.
    sink = stream_sink.get()
    if sink is not None:
        parts: list[str] = []
        try:
            async for delta in llm_client.stream_once(messages):
                parts.append(delta)
                await sink.put(delta)
        except Exception as exc:
            logger.warning(f"[{state['session_id']}] response_generator | stream_error={exc!r}")
        llm_reply = "".join(parts).strip()
        if llm_reply:
            return llm_reply
    else:
        llm_reply = await llm_client.chat_complete(messages)
        if llm_reply:
            return llm_reply.strip()

    # Fallback to legacy template
    logger.info(f"[{state['session_id']}] response_generator | llm_failed intent={intent} using fallback")
    fallback = _FALLBACK_BUILDERS.get(intent, _FALLBACK_BUILDERS["UNKNOWN"])
    return fallback(result)
