from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Awaitable, Callable

from app.clients.backend_client import BackendClient
from app.clients.forecasting_client import ForecastingClient
from app.config.settings import settings
from app.policy.capability_policy import assert_controlled_ai_job_tool, assert_read_only_tool

ToolHandler = Callable[[str, dict[str, Any]], Awaitable[Any]]


@dataclass(frozen=True)
class ToolDefinition:
    name: str
    source: str
    handler: ToolHandler


class ToolRegistry:
    def __init__(self, forecasting: ForecastingClient | None = None, backend: BackendClient | None = None):
        self.forecasting = forecasting or ForecastingClient()
        self.backend = backend or BackendClient()
        self._tools: dict[str, ToolDefinition] = {
            "get_data_quality_summary": ToolDefinition("get_data_quality_summary", "forecasting", self._data_quality),
            "get_inventory_risks": ToolDefinition("get_inventory_risks", "forecasting", self._inventory_risks),
            "get_inventory_risk_detail": ToolDefinition("get_inventory_risk_detail", "forecasting", self._risk_detail),
            "get_replenishment_suggestions": ToolDefinition("get_replenishment_suggestions", "forecasting", self._suggestions),
            "get_replenishment_detail": ToolDefinition("get_replenishment_detail", "forecasting", self._suggestion_detail),
            "get_forecast_quality": ToolDefinition("get_forecast_quality", "forecasting", self._data_quality),
            "get_sales_overview": ToolDefinition("get_sales_overview", "backend", self._sales_overview),
            "get_product_performance": ToolDefinition("get_product_performance", "backend", self._product_performance),
            "get_order_overview": ToolDefinition("get_order_overview", "backend", self._order_overview),
            "simulate_inventory_policy": ToolDefinition("simulate_inventory_policy", "forecasting", self._simulate),
            "search_product_inventory": ToolDefinition("search_product_inventory", "backend", self._inventory_lookup),
            "get_best_selling_products": ToolDefinition("get_best_selling_products", "backend", self._best_sellers),
            "get_ai_data_freshness": ToolDefinition("get_ai_data_freshness", "forecasting", self._freshness),
            "get_urgent_replenishment_candidates": ToolDefinition("get_urgent_replenishment_candidates", "forecasting", self._urgent_replenishment),
            "sync_ai_snapshot": ToolDefinition("sync_ai_snapshot", "forecasting", self._sync_snapshot),
            "run_demand_classification": ToolDefinition("run_demand_classification", "forecasting", self._run_demand_classification),
            "run_forecast_evaluation": ToolDefinition("run_forecast_evaluation", "forecasting", self._run_evaluation),
            "run_forecast_generation": ToolDefinition("run_forecast_generation", "forecasting", self._run_generation),
            "get_ai_job_status": ToolDefinition("get_ai_job_status", "forecasting", self._job_status),
        }

    def get(self, name: str) -> ToolDefinition:
        if name.startswith(("sync_", "run_")) or name == "get_ai_job_status":
            assert_controlled_ai_job_tool(name, settings.CONTROLLED_AI_JOBS_ENABLED or name == "get_ai_job_status")
        else:
            assert_read_only_tool(name)
        return self._tools[name]

    async def execute(self, name: str, token: str, args: dict[str, Any]) -> tuple[Any, str]:
        tool = self.get(name)
        return await tool.handler(token, args), tool.source

    async def _data_quality(self, token: str, args: dict[str, Any]) -> Any:
        return await self.forecasting.data_quality_summary(token)

    async def _inventory_risks(self, token: str, args: dict[str, Any]) -> Any:
        return await self.forecasting.inventory_risks(token, args.get("risk"))

    async def _risk_detail(self, token: str, args: dict[str, Any]) -> Any:
        return await self.forecasting.inventory_risk_detail(token, args["variantId"])

    async def _suggestions(self, token: str, args: dict[str, Any]) -> Any:
        return await self.forecasting.replenishment_suggestions(token, **args)

    async def _suggestion_detail(self, token: str, args: dict[str, Any]) -> Any:
        recommendation_id = args["recommendationId"]
        detail = await self.forecasting.replenishment_detail(token, recommendation_id)
        explanation = await self.forecasting.replenishment_explanation(token, recommendation_id)
        return {"detail": detail, "explanation": explanation}

    async def _simulate(self, token: str, args: dict[str, Any]) -> Any:
        return await self.forecasting.simulate_inventory_policy(token, args)

    async def _sales_overview(self, token: str, args: dict[str, Any]) -> Any:
        return await self.backend.sales_overview(token)

    async def _product_performance(self, token: str, args: dict[str, Any]) -> Any:
        return await self.backend.product_performance(token)

    async def _order_overview(self, token: str, args: dict[str, Any]) -> Any:
        return await self.backend.order_overview(token)

    async def _inventory_lookup(self, token: str, args: dict[str, Any]) -> Any:
        if not settings.PRODUCT_LOOKUP_ENABLED:
            raise PermissionError("Product inventory lookup is disabled")
        return await self.backend.inventory_lookup(
            token,
            query=args.get("query"),
            sku=args.get("sku"),
            variant_id=args.get("variantId"),
            limit=min(int(args.get("limit", 20)), settings.MAX_AGENT_RESULT_ROWS),
        )

    async def _best_sellers(self, token: str, args: dict[str, Any]) -> Any:
        if not settings.BEST_SELLER_LOOKUP_ENABLED:
            raise PermissionError("Best-seller lookup is disabled")
        from datetime import date, timedelta

        to_date = date.fromisoformat(args["toDate"]) if args.get("toDate") else date.today()
        from_date = date.fromisoformat(args["fromDate"]) if args.get("fromDate") else to_date - timedelta(days=29)
        return await self.backend.best_sellers(
            token,
            from_date=from_date,
            to_date=to_date,
            limit=min(int(args.get("limit", 10)), settings.MAX_AGENT_RESULT_ROWS),
        )

    async def _freshness(self, token: str, args: dict[str, Any]) -> Any:
        return await self.forecasting.freshness(token, args.get("dataSource"))

    async def _urgent_replenishment(self, token: str, args: dict[str, Any]) -> Any:
        limit = min(int(args.get("limit", 5)), settings.MAX_AGENT_RESULT_ROWS)
        suggestions = await self.forecasting.replenishment_suggestions(token, status="PENDING", limit=max(limit, 20))
        content = suggestions.get("content", []) if isinstance(suggestions, dict) else []
        priority_order = {"CRITICAL": 0, "HIGH": 1, "MEDIUM": 2, "LOW": 3}
        ranked = sorted(
            content,
            key=lambda item: (
                priority_order.get(str(item.get("priority", "")).upper(), 9),
                -int(item.get("suggestedQuantity") or 0),
            ),
        )[:limit]
        return {"limit": limit, "content": ranked, "source": "pending_replenishment_suggestions"}

    async def _sync_snapshot(self, token: str, args: dict[str, Any]) -> Any:
        _assert_allowed_data_source(args)
        return await self.forecasting.sync_snapshot(token, args)

    async def _run_demand_classification(self, token: str, args: dict[str, Any]) -> Any:
        _assert_allowed_data_source(args)
        return await self.forecasting.run_demand_classification(token, args)

    async def _run_evaluation(self, token: str, args: dict[str, Any]) -> Any:
        _assert_allowed_data_source(args)
        return await self.forecasting.run_forecast_evaluation(token, args)

    async def _run_generation(self, token: str, args: dict[str, Any]) -> Any:
        _assert_allowed_data_source(args)
        return await self.forecasting.run_forecast_generation(token, args)

    async def _job_status(self, token: str, args: dict[str, Any]) -> Any:
        return await self.forecasting.job_status(token, args.get("jobId", "current"))


def _assert_allowed_data_source(args: dict[str, Any]) -> None:
    data_source = str(args.get("dataSource") or "DEMO").upper()
    allowed = {value.strip().upper() for value in settings.AI_JOB_ALLOWED_DATA_SOURCES.split(",") if value.strip()}
    if data_source not in allowed:
        raise PermissionError(f"AI job dataSource {data_source} is not allowed")
