from __future__ import annotations

from typing import Any
from uuid import uuid4

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

    async def freshness(self, token: str, data_source: str | None = None) -> dict[str, Any]:
        params = {"dataSource": data_source} if data_source else None
        return await self.request("GET", "/api/v1/admin/ai/freshness", token, params=params)

    async def sync_snapshot(self, token: str, payload: dict[str, Any]) -> dict[str, Any]:
        body = {"correlationId": payload.get("correlationId") or str(uuid4()), "variantIds": payload.get("variantIds", [])}
        return await self.request("POST", "/api/v1/admin/replenishment/snapshots/sync", token, json=body)

    async def run_demand_classification(self, token: str, payload: dict[str, Any]) -> dict[str, Any]:
        params: dict[str, Any] = {}
        if payload.get("dataSource"):
            params["dataSource"] = payload["dataSource"]
        return await self.request("POST", "/api/v1/admin/ai/demand-classifications/run", token, params=params)

    async def run_forecast_evaluation(self, token: str, payload: dict[str, Any]) -> dict[str, Any]:
        return await self.request("POST", "/api/v1/admin/replenishment/evaluate", token, json=payload)

    async def run_forecast_generation(self, token: str, payload: dict[str, Any]) -> dict[str, Any]:
        return await self.request("POST", "/api/v1/admin/replenishment/generate", token, json=payload)

    async def job_status(self, token: str, job_id: str) -> dict[str, Any]:
        return await self.request("GET", f"/api/v1/admin/ai/jobs/{job_id}", token)
