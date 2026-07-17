"""
LLM-powered tool selection for intent=UNKNOWN.

When rule-based intent routing cannot classify a message, this module calls
OpenAI with function-calling to select the best informational tool.

Scope (intentionally limited):
  - Only informational tools: search, detail, recommend, size_advisor, knowledge, order_status
  - Does NOT select add_to_cart or cancel_order (those require deterministic intent signals)
  - Falls back to ("none", {}) on any failure: no API key, LLM error, no tool call returned

Security:
  - access_token is NEVER passed to the LLM; injected from current request after selection
  - Input is the raw user message only; no session state is sent to the API
"""
from __future__ import annotations

from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

# Tool function definitions for OpenAI function calling
_TOOLS: list[dict] = [
    {
        "type": "function",
        "function": {
            "name": "search_products",
            "description": "Tìm kiếm sản phẩm theo từ khóa, loại sản phẩm, môn thể thao, giới tính, khoảng giá.",
            "parameters": {
                "type": "object",
                "properties": {
                    "query": {"type": "string", "description": "Từ khóa tìm kiếm từ yêu cầu của người dùng"}
                },
                "required": ["query"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_product_detail",
            "description": "Xem thông tin chi tiết, giá, size, màu sắc, tồn kho của một sản phẩm cụ thể.",
            "parameters": {
                "type": "object",
                "properties": {
                    "product_name": {"type": "string", "description": "Tên hoặc mô tả sản phẩm người dùng hỏi về"},
                    "size_hint":    {"type": "string", "description": "Size cụ thể nếu người dùng đề cập"},
                    "color_hint":   {"type": "string", "description": "Màu sắc cụ thể nếu người dùng đề cập"},
                },
                "required": [],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "recommend_products",
            "description": "Gợi ý sản phẩm phù hợp theo nhu cầu hoặc tương tự sản phẩm đang xem.",
            "parameters": {
                "type": "object",
                "properties": {
                    "query": {"type": "string", "description": "Mô tả nhu cầu của người dùng"}
                },
                "required": ["query"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "size_advisor",
            "description": "Tư vấn size giày/quần áo phù hợp dựa trên số đo cơ thể hoặc tên sản phẩm.",
            "parameters": {
                "type": "object",
                "properties": {
                    "message": {"type": "string", "description": "Câu hỏi về size của người dùng"}
                },
                "required": ["message"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "answer_knowledge",
            "description": "Trả lời câu hỏi về chính sách, đổi trả, giao hàng, thanh toán, bảo hành của shop.",
            "parameters": {
                "type": "object",
                "properties": {
                    "query": {"type": "string", "description": "Câu hỏi về chính sách hoặc thông tin shop"}
                },
                "required": ["query"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_order_status",
            "description": "Kiểm tra trạng thái đơn hàng, thông tin vận chuyển, thanh toán.",
            "parameters": {
                "type": "object",
                "properties": {
                    "order_code": {"type": "string", "description": "Mã đơn hàng (ví dụ: DH001, DH-1234)"}
                },
                "required": [],
            },
        },
    },
]

_SYSTEM_BASE = (
    "Bạn là bộ phân loại yêu cầu cho chatbot bán hàng thể thao. "
    "Phân tích tin nhắn của khách và gọi đúng một hàm phù hợp nhất. "
    "Nếu không rõ yêu cầu thuộc loại nào, KHÔNG gọi hàm."
)


def _build_system_prompt() -> str:
    """Build system prompt with live catalog context when available."""
    try:
        from app.services.catalog_context import get_catalog_context
        catalog = get_catalog_context()
        if catalog:
            return f"{_SYSTEM_BASE}\n\nDanh mục thực tế của cửa hàng:\n{catalog}"
    except Exception:
        pass
    return _SYSTEM_BASE


async def select_tool_for_unknown(
    message: str,
    session_context: dict,
    access_token: str | None,
) -> tuple[str, dict]:
    """
    Use LLM function calling to select the best tool for an ambiguous message.

    Returns (tool_name, tool_args) where tool_args NEVER contains access_token
    (that is injected later by tool_selector_node from the current request).

    Returns ("none", {}) when: no API key, LLM failure, or no tool call chosen.
    """
    from app.services import llm_client

    if not llm_client.is_available():
        logger.info("tool_selector_llm | skip reason=llm_unavailable")
        return "none", {}

    system_prompt = _build_system_prompt()

    selected = await llm_client.select_tool(system_prompt, message, _TOOLS)
    if not selected:
        logger.info("tool_selector_llm | no_function_call_returned")
        return "none", {}
    fn_name, fn_args = selected

    logger.info(f"tool_selector_llm | selected={fn_name} args={list(fn_args.keys())}")

    # Map LLM args to internal tool arg format
    tool_args = _build_args(fn_name, fn_args, message, session_context, access_token)
    return fn_name, tool_args


def _build_args(
    tool: str,
    fn_args: dict,
    message: str,
    ctx: dict,
    access_token: str | None,
) -> dict:
    if tool == "search_products":
        return {"query": fn_args.get("query") or message}

    if tool == "get_product_detail":
        from app.memory import context_resolver
        from app.graph.state import AgentState  # type: ignore[attr-defined]

        # Try to resolve product from session context first
        product_id = context_resolver.resolve_product_reference(message, ctx) or ""
        return {
            "product_id": product_id,
            "size_hint":  fn_args.get("size_hint"),
            "color_hint": fn_args.get("color_hint"),
        }

    if tool == "recommend_products":
        selected_id = ctx.get("selected_product_id") or ""
        mode = "related" if selected_id else "need_based"
        return {"mode": mode, "product_id": selected_id, "query": fn_args.get("query") or message}

    if tool == "size_advisor":
        from app.memory import context_resolver
        product_id = context_resolver.resolve_product_reference(message, ctx)
        return {"message": fn_args.get("message") or message, "product_id": product_id}

    if tool == "answer_knowledge":
        return {"query": fn_args.get("query") or message}

    if tool == "get_order_status":
        import re
        code_match = re.search(r"\b(DH[-]?\d+)\b", message, re.IGNORECASE)
        order_code = fn_args.get("order_code") or (code_match.group(1).upper() if code_match else "")
        return {"order_code": order_code, "access_token": access_token}

    return {}
