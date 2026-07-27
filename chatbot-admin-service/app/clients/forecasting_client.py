from __future__ import annotations

from typing import Any

from app.clients.base import ApiClient
from app.config.settings import settings


class ForecastingClient(ApiClient):
    def __init__(self) -> None:
        super().__init__(str(settings.FORECASTING_API_BASE_URL))

    async def data_quality_summary(self, token: str) -> dict[str, Any]:
        return await self.request("GET", "/api/v1/admin/ai/data-quality/summary", token)

    async def inventory_risks(self, token: str, risk: str | None = None) -> list[dict[str, Any]]:
        params = {"risk": risk} if risk else None
        return await self.request("GET", "/api/v1/admin/ai/inventory-risks", token, params=params)

    async def inventory_risk_detail(self, token: str, variant_id: str) -> dict[str, Any]:
        return await self.request("GET", f"/api/v1/admin/ai/inventory-risks/{variant_id}", token)

    async def replenishment_suggestions(
        self,
        token: str,
        *,
        status: str | None = "PENDING",
        priority: str | None = None,
        keyword: str | None = None,
        limit: int = 20,
    ) -> dict[str, Any]:
        params = {"limit": limit}
        if status:
            params["status"] = status
        if priority:
            params["priority"] = priority
        if keyword:
            params["keyword"] = keyword
        return await self.request("GET", "/api/v1/admin/replenishment/suggestions", token, params=params)

    async def replenishment_detail(self, token: str, recommendation_id: str) -> dict[str, Any]:
        return await self.request("GET", f"/api/v1/admin/replenishment/suggestions/{recommendation_id}", token)

    async def replenishment_explanation(self, token: str, recommendation_id: str) -> dict[str, Any]:
        return await self.request("GET", f"/api/v1/admin/ai/replenishment/explanations/{recommendation_id}", token)

    async def simulate_inventory_policy(self, token: str, payload: dict[str, Any]) -> dict[str, Any]:
        return await self.request("POST", "/api/v1/admin/ai/inventory/simulate", token, json=payload)

    async def accept_replenishment(self, token: str, recommendation_id: str, payload: dict[str, Any]) -> dict[str, Any]:
        return await self.request("POST", f"/api/v1/admin/replenishment/suggestions/{recommendation_id}/accept", token, json=payload)

    async def adjust_replenishment(self, token: str, recommendation_id: str, payload: dict[str, Any]) -> dict[str, Any]:
        return await self.request("POST", f"/api/v1/admin/replenishment/suggestions/{recommendation_id}/adjust", token, json=payload)

    async def dismiss_replenishment(self, token: str, recommendation_id: str, payload: dict[str, Any]) -> dict[str, Any]:
        return await self.request("POST", f"/api/v1/admin/replenishment/suggestions/{recommendation_id}/dismiss", token, json=payload)
