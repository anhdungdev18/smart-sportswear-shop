import asyncio

from app.graph.routing import classify_intent, classify_message, classify_message_intelligently, select_tool


def test_inventory_risk_routes_to_risk_tool():
    intent = classify_intent("SKU nào có rủi ro stockout?")

    assert intent == "INVENTORY_RISK"
    assert select_tool(intent) == "get_inventory_risks"


def test_unknown_falls_back_to_data_quality():
    intent = classify_intent("xin chào")

    assert intent == "UNKNOWN"
    assert select_tool(intent) == "get_data_quality_summary"


def test_phase9_inventory_lookup_routes_to_lookup_tool():
    intent = classify_intent("SKU ABC con bao nhieu hang?")

    assert intent == "PRODUCT_INVENTORY_LOOKUP"
    assert select_tool(intent) == "search_product_inventory"


def test_vietnamese_order_count_question_routes_to_order_overview():
    intent = classify_intent("hi\u1ec7n t\u1ea1i c\u00f3 bao nhi\u00eau \u0111\u01a1n h\u00e0ng")

    assert intent == "ORDER_OVERVIEW"
    assert select_tool(intent) == "get_order_overview"


def test_phase9_best_seller_routes_to_best_seller_tool():
    intent = classify_intent("Top 10 SKU ban chay 30 ngay qua")

    assert intent == "BEST_SELLING_PRODUCTS"
    assert select_tool(intent) == "get_best_selling_products"


def test_phase9_refresh_routes_to_freshness_gate_first():
    intent = classify_intent("Chay lai forecast roi cho toi top can nhap")

    assert intent == "AI_PIPELINE_REFRESH"
    assert select_tool(intent) == "get_ai_data_freshness"


def test_classification_adds_question_type_for_revenue_explanation():
    result = classify_message("Tại sao doanh thu ghi nhận và thực nhận chênh nhau?")

    assert result.intent == "SALES_OVERVIEW"
    assert result.questionType == "EXPLANATION"
    assert result.neededTools == ["get_revenue_breakdown"]


class FakeLlm:
    def __init__(self, payload):
        self.payload = payload

    def enabled(self):
        return True

    async def complete_json(self, system_prompt, user_payload):
        return self.payload


def test_llm_classifier_accepts_valid_json_schema():
    result = asyncio.run(
        classify_message_intelligently(
            "Don hang hom nay co bat thuong khong?",
            available_tools=["get_order_overview", "get_order_status_trend"],
            llm=FakeLlm(
                {
                    "intent": "ORDER_OVERVIEW",
                    "questionType": "DIAGNOSIS",
                    "neededTools": ["get_order_overview", "get_order_status_trend"],
                    "entities": {},
                    "timeRange": None,
                    "confidence": 0.91,
                    "clarifyingQuestion": None,
                }
            ),
        )
    )

    assert result.intent == "ORDER_OVERVIEW"
    assert result.questionType == "DIAGNOSIS"
    assert result.neededTools == ["get_order_overview", "get_order_status_trend"]


def test_llm_classifier_rejects_invented_tool_and_falls_back():
    result = asyncio.run(
        classify_message_intelligently(
            "Doanh thu hien tai bao nhieu?",
            available_tools=["get_sales_overview"],
            llm=FakeLlm(
                {
                    "intent": "SALES_OVERVIEW",
                    "questionType": "METRIC",
                    "neededTools": ["drop_database"],
                    "entities": {},
                    "confidence": 0.99,
                }
            ),
        )
    )

    assert result.intent == "SALES_OVERVIEW"
    assert result.neededTools == ["get_sales_overview"]
