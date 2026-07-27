from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Awaitable, Callable

from app.clients.backend_client import BackendClient
from app.clients.forecasting_client import ForecastingClient
from app.policy.capability_policy import assert_read_only_tool

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
        }

    def get(self, name: str) -> ToolDefinition:
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
