"""
normalize node — parse query một lần duy nhất ở đầu pipeline.

Kết quả lưu vào state["parsed_query"] và state["normalized_query"].
Các node sau (tool_selector, search_service) dùng lại, tránh parse lại.
Giống DigiAI normalize_and_parse node.
"""
from __future__ import annotations

from app.graph.state import AgentState
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)


async def normalize_node(state: AgentState) -> dict:
    message = state.get("message", "")

    try:
        from app.retrieval.product.parser.query_parser import parse_query
        parsed = parse_query(message)
        return {
            "normalized_query": parsed.normalized,
            "parsed_query": {
                "keyword":         parsed.keyword,
                "normalized":      parsed.normalized,
                "product_type":    parsed.product_type,
                "sport_type_hint": parsed.sport_type_hint,
                "gender":          parsed.gender,
                "brand":           parsed.brand,
                "color":           parsed.color,
                "price_min":       parsed.price_min,
                "price_max":       parsed.price_max,
                "feature_hints":   parsed.feature_hints,
            },
        }
    except Exception as exc:
        logger.warning(f"[{state.get('session_id')}] normalize | parse_error={exc!r}")
        return {
            "normalized_query": message.lower().strip(),
            "parsed_query":     {},
        }
