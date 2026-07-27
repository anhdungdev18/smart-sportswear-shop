from __future__ import annotations

import unicodedata

from app.services.llm_client import LlmClient
from app.schemas.chat import ClassificationResult, Intent, QuestionType


def _normalize_text(message: str) -> str:
    message = message.replace("đ", "d").replace("Đ", "D")
    decomposed = unicodedata.normalize("NFD", message.lower())
    without_marks = "".join(char for char in decomposed if unicodedata.category(char) != "Mn")
    return without_marks.replace("đ", "d")


def classify_intent(message: str) -> Intent:
    text = _normalize_text(message)
    if any(term in text for term in ["freshness", "stale", "du lieu forecast", "cu khong"]):
        return "AI_DATA_FRESHNESS"
    if any(term in text for term in ["chay lai", "refresh", "sync", "evaluate", "generate forecast"]):
        return "AI_PIPELINE_REFRESH"
    if any(term in text for term in ["ban chay", "best seller", "top sku", "top 10 sku"]):
        return "BEST_SELLING_PRODUCTS"
    if any(term in text for term in ["can nhap cap", "nhap cap", "urgent"]):
        return "URGENT_REPLENISHMENT_ANALYSIS"
    if any(term in text for term in ["con bao nhieu", "con ton", "variant"]):
        return "PRODUCT_INVENTORY_LOOKUP"
    if "canh bao" in text:
        return "INVENTORY_RISK"
    if "sku" in text and "ton kho" in text:
        return "PRODUCT_INVENTORY_LOOKUP"
    if any(term in text for term in ["what-if", "simulate", "mo phong", "gia lap"]):
        return "WHAT_IF_SIMULATION"
    if any(term in text for term in ["forecast", "wape", "confidence", "data quality", "chat luong", "du bao", "do tin cay"]):
        return "FORECAST_QUALITY"
    if any(term in text for term in ["explain", "giai thich", "replenishment", "de xuat nhap", "nhap hang", "goi y nhap"]):
        return "REPLENISHMENT_EXPLANATION"
    if any(term in text for term in ["stockout", "overstock", "ton kho", "risk", "rui ro", "sap het hang", "ton kho thap"]):
        return "INVENTORY_RISK"
    if any(term in text for term in ["doanh thu", "revenue", "sales", "ban hang"]):
        return "SALES_OVERVIEW"
    if any(term in text for term in ["san pham", "product performance", "sku ban", "mat hang"]):
        return "PRODUCT_PERFORMANCE"
    if "sku" in text:
        return "PRODUCT_INVENTORY_LOOKUP"
    if any(term in text for term in ["don hang", "order", "orders"]):
        return "ORDER_OVERVIEW"
    return "UNKNOWN"

def classify_message(message: str, conversation_summary: str | None = None) -> ClassificationResult:
    """Deterministic classifier fallback with question type and simple entity extraction."""
    intent = classify_intent(message)
    question_type = classify_question_type(message, conversation_summary)
    if intent == "UNKNOWN" and question_type in {"FOLLOW_UP", "EXPLANATION", "COMPARISON", "DIAGNOSIS"} and conversation_summary:
        intent = _intent_from_summary(conversation_summary)
    tool = select_tool(intent)
    needed_tools = [] if intent == "UNKNOWN" else [tool]

    text = _normalize_text(message)
    if intent == "SALES_OVERVIEW" and question_type in {"EXPLANATION", "COMPARISON", "DIAGNOSIS"}:
        needed_tools = ["get_revenue_breakdown"]
    elif intent == "ORDER_OVERVIEW" and question_type in {"COMPARISON", "DIAGNOSIS"}:
        needed_tools = ["get_order_overview", "get_order_status_trend"]

    clarifying = None
    confidence = 0.92 if intent != "UNKNOWN" else 0.35
    if intent == "UNKNOWN":
        clarifying = "Bạn muốn xem nghiệp vụ nào: đơn hàng, doanh thu, tồn kho, dự báo hay nhập hàng?"
    if question_type == "FOLLOW_UP" and conversation_summary:
        confidence = max(confidence, 0.75)

    return ClassificationResult(
        intent=intent,
        questionType=question_type,
        neededTools=needed_tools,
        entities=_extract_entities(message),
        confidence=confidence,
        clarifyingQuestion=clarifying,
    )


async def classify_message_intelligently(
    message: str,
    conversation_summary: str | None = None,
    available_tools: list[str] | None = None,
    llm: LlmClient | None = None,
) -> ClassificationResult:
    fallback = classify_message(message, conversation_summary)
    client = llm or LlmClient()
    if not client.enabled():
        return fallback

    allowed_tools = available_tools or _default_available_tools()
    payload = {
        "message": message,
        "conversationSummary": conversation_summary,
        "availableTools": allowed_tools,
        "allowedIntents": list(Intent.__args__),  # type: ignore[attr-defined]
        "allowedQuestionTypes": list(QuestionType.__args__),  # type: ignore[attr-defined]
    }
    system_prompt = (
        "You classify Vietnamese admin operations questions for a sportswear ecommerce admin copilot. "
        "Return only valid JSON matching this shape: intent, questionType, neededTools, entities, "
        "timeRange, confidence, clarifyingQuestion. Do not invent tools. Choose only from availableTools. "
        "If confidence is below 0.7, include a concise Vietnamese clarifyingQuestion."
    )
    try:
        parsed = await client.complete_json(system_prompt, payload)
        result = ClassificationResult.model_validate(parsed)
    except Exception:
        return fallback

    if any(tool not in allowed_tools for tool in result.neededTools):
        return fallback
    if result.confidence < 0.7 and not result.clarifyingQuestion:
        return fallback
    return result


def _default_available_tools() -> list[str]:
    tools = {select_tool(intent) for intent in Intent.__args__ if intent != "UNKNOWN"}  # type: ignore[attr-defined]
    tools.update({"get_revenue_breakdown", "get_order_status_trend", "get_data_quality_summary"})
    return sorted(tools)


def _intent_from_summary(summary: str) -> Intent:
    for intent in [
        "SALES_OVERVIEW",
        "ORDER_OVERVIEW",
        "INVENTORY_RISK",
        "PRODUCT_INVENTORY_LOOKUP",
        "BEST_SELLING_PRODUCTS",
        "FORECAST_QUALITY",
        "REPLENISHMENT_EXPLANATION",
        "URGENT_REPLENISHMENT_ANALYSIS",
        "AI_DATA_FRESHNESS",
        "AI_PIPELINE_REFRESH",
        "WHAT_IF_SIMULATION",
    ]:
        if intent in summary:
            return intent  # type: ignore[return-value]
    return "UNKNOWN"


def classify_question_type(message: str, conversation_summary: str | None = None) -> QuestionType:
    text = _normalize_text(message)
    if any(term in text for term in ["chay lai", "refresh", "sync", "cap nhat", "generate", "evaluate"]):
        return "ACTION_REQUEST"
    if any(term in text for term in ["tai sao", "vi sao", "giai thich", "nguyen nhan", "khac nhau", "chenh"]):
        return "EXPLANATION"
    if any(term in text for term in ["so sanh", "hon", "kem", "thang truoc", "tuan truoc", "hom qua"]):
        return "COMPARISON"
    if any(term in text for term in ["bat thuong", "rui ro", "dang lo", "van de", "canh bao", "stockout", "overstock"]):
        return "DIAGNOSIS"
    if any(term in text for term in ["nen", "can lam gi", "uu tien", "de xuat", "goi y"]):
        return "RECOMMENDATION"
    if any(term in text for term in ["bao nhieu", "tong", "hien co", "hien tai co", "doanh thu", "so luong"]):
        return "METRIC"
    if any(term in text for term in ["sku", "ma don", "order code", "chi tiet", "variant"]):
        return "DETAIL_LOOKUP"
    if any(term in text for term in ["tai sao lai", "vay", "no", "cai do", "muc do"]) and conversation_summary:
        return "FOLLOW_UP"
    return "UNKNOWN"


def _extract_entities(message: str) -> dict[str, str]:
    import re

    entities: dict[str, str] = {}
    sku_match = re.search(r"\b[A-Z0-9][A-Z0-9_-]{3,}\b", message.upper())
    if sku_match:
        entities["sku"] = sku_match.group(0)
    return entities


def select_tool(intent: Intent) -> str:
    return {
        "INVENTORY_RISK": "get_inventory_risks",
        "REPLENISHMENT_EXPLANATION": "get_replenishment_suggestions",
        "FORECAST_QUALITY": "get_forecast_quality",
        "SALES_OVERVIEW": "get_sales_overview",
        "PRODUCT_PERFORMANCE": "get_product_performance",
        "ORDER_OVERVIEW": "get_order_overview",
        "WHAT_IF_SIMULATION": "simulate_inventory_policy",
        "PRODUCT_INVENTORY_LOOKUP": "search_product_inventory",
        "BEST_SELLING_PRODUCTS": "get_best_selling_products",
        "URGENT_REPLENISHMENT_ANALYSIS": "get_urgent_replenishment_candidates",
        "AI_PIPELINE_REFRESH": "get_ai_data_freshness",
        "AI_DATA_FRESHNESS": "get_ai_data_freshness",
    }.get(intent, "get_data_quality_summary")
