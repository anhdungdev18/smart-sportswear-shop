from __future__ import annotations

from app.schemas.chat import Intent


def classify_intent(message: str) -> Intent:
    text = message.lower()
    if any(term in text for term in ["what-if", "simulate", "mô phỏng", "mo phong", "gia lap", "giả lập"]):
        return "WHAT_IF_SIMULATION"
    if any(term in text for term in ["forecast", "wape", "confidence", "data quality", "chất lượng", "chat luong", "du bao", "do tin cay"]):
        return "FORECAST_QUALITY"
    if any(term in text for term in ["explain", "giải thích", "giai thich", "replenishment", "đề xuất nhập", "de xuat nhap", "nhap hang", "goi y nhap"]):
        return "REPLENISHMENT_EXPLANATION"
    if any(term in text for term in ["stockout", "overstock", "tồn kho", "ton kho", "risk", "rủi ro", "rui ro", "sắp hết hàng", "sap het hang", "ton kho thap"]):
        return "INVENTORY_RISK"
    if any(term in text for term in ["doanh thu", "revenue", "sales", "ban hang"]):
        return "SALES_OVERVIEW"
    if any(term in text for term in ["sản phẩm", "san pham", "product performance", "sku bán", "sku ban", "ban chay", "mat hang"]):
        return "PRODUCT_PERFORMANCE"
    if any(term in text for term in ["đơn hàng", "don hang", "order", "orders"]):
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
    }.get(intent, "get_data_quality_summary")
