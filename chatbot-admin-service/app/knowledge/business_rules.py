from __future__ import annotations

from app.schemas.chat import Intent


BUSINESS_RULES: dict[str, list[str]] = {
    "SALES_OVERVIEW": [
        "grossRevenue is the sum of totalAmount for orders with paymentStatus=PAID.",
        "realizedRevenue is the sum of totalAmount for orders with orderStatus=DELIVERED.",
        "These values can differ because payment and delivery statuses describe different slices.",
        "If detailed breakdown is missing, explain possibilities rather than definitive causes.",
    ],
    "ORDER_OVERVIEW": [
        "Total orders comes from the order overview report.",
        "Metric questions should be concise.",
        "Diagnosis questions need status and time breakdown before strong conclusions.",
    ],
    "INVENTORY_RISK": [
        "availableQuantity equals stockQuantity minus reservedQuantity.",
        "Stockout risks should be prioritized for replenishment review.",
        "Overstock is excess inventory risk and should not trigger automatic replenishment.",
    ],
    "FORECAST_QUALITY": [
        "Stale AI data should be warned about before strong operational conclusions.",
        "Low forecast quality means results should be used cautiously.",
    ],
}


def business_rules_for(intent: Intent) -> list[str]:
    return BUSINESS_RULES.get(intent, [])
