READ_ONLY_TOOLS = {
    "get_data_quality_summary",
    "get_inventory_risks",
    "get_inventory_risk_detail",
    "get_inventory_risk_explanation",
    "get_replenishment_suggestions",
    "get_replenishment_detail",
    "get_forecast_quality",
    "get_sales_overview",
    "get_revenue_breakdown",
    "get_order_status_trend",
    "get_product_performance",
    "get_order_overview",
    "simulate_inventory_policy",
    "search_product_inventory",
    "get_best_selling_products",
    "get_ai_data_freshness",
    "get_urgent_replenishment_candidates",
}

CONTROLLED_AI_JOB_TOOLS = {
    "sync_ai_snapshot",
    "run_demand_classification",
    "run_forecast_evaluation",
    "run_forecast_generation",
    "get_ai_job_status",
}


def assert_read_only_tool(tool_name: str) -> None:
    if tool_name not in READ_ONLY_TOOLS:
        raise PermissionError(f"Tool is not allowlisted: {tool_name}")


def assert_controlled_ai_job_tool(tool_name: str, enabled: bool) -> None:
    if tool_name not in CONTROLLED_AI_JOB_TOOLS:
        raise PermissionError(f"Tool is not a controlled AI job: {tool_name}")
    if not enabled:
        raise PermissionError("Controlled AI job orchestration is disabled")
