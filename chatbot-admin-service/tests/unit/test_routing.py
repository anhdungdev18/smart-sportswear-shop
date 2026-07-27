from app.graph.routing import classify_intent, select_tool


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


def test_phase9_best_seller_routes_to_best_seller_tool():
    intent = classify_intent("Top 10 SKU ban chay 30 ngay qua")

    assert intent == "BEST_SELLING_PRODUCTS"
    assert select_tool(intent) == "get_best_selling_products"


def test_phase9_refresh_routes_to_freshness_gate_first():
    intent = classify_intent("Chay lai forecast roi cho toi top can nhap")

    assert intent == "AI_PIPELINE_REFRESH"
    assert select_tool(intent) == "get_ai_data_freshness"
