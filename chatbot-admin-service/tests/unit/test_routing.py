from app.graph.routing import classify_intent, select_tool


def test_inventory_risk_routes_to_risk_tool():
    intent = classify_intent("SKU nào có rủi ro stockout?")

    assert intent == "INVENTORY_RISK"
    assert select_tool(intent) == "get_inventory_risks"


def test_unknown_falls_back_to_data_quality():
    intent = classify_intent("xin chào")

    assert intent == "UNKNOWN"
    assert select_tool(intent) == "get_data_quality_summary"
