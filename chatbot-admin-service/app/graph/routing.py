from __future__ import annotations

from app.schemas.chat import Intent


def classify_intent(message: str) -> Intent:
    text = message.lower()
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
