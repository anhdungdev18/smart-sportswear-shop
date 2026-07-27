READ_ONLY_TOOLS = {
    "get_data_quality_summary",
    "get_inventory_risks",
    "get_inventory_risk_detail",
    "get_replenishment_suggestions",
    "get_replenishment_detail",
    "get_forecast_quality",
    "get_sales_overview",
    "get_product_performance",
    "get_order_overview",
    "simulate_inventory_policy",
}


def assert_read_only_tool(tool_name: str) -> None:
    if tool_name not in READ_ONLY_TOOLS:
        raise PermissionError(f"Tool is not allowlisted: {tool_name}")
