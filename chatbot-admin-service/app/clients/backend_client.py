from __future__ import annotations

from typing import Any
from datetime import date

from app.clients.base import ApiClient
from app.config.settings import settings


class BackendClient(ApiClient):
    def __init__(self) -> None:
        super().__init__(str(settings.CORE_BACKEND_API_BASE_URL))

    async def sales_overview(self, token: str) -> dict[str, Any]:
        return await self.request("GET", "/api/v1/admin/reports/overview", token)

    async def revenue_breakdown(self, token: str) -> dict[str, Any]:
        try:
            return await self.request("GET", "/api/v1/admin/reports/revenue/breakdown", token)
        except Exception:
            overview = await self.sales_overview(token)
            gross = overview.get("grossRevenue")
            realized = overview.get("realizedRevenue")
            difference = None
            if isinstance(gross, (int, float)) and isinstance(realized, (int, float)):
                difference = realized - gross
            return {
                "grossRevenue": gross,
                "realizedRevenue": realized,
                "difference": difference,
                "breakdownAvailable": False,
            }

    async def order_status_trend(self, token: str) -> dict[str, Any]:
        try:
            return await self.request("GET", "/api/v1/admin/reports/orders/status-trend", token)
        except Exception:
            overview = await self.order_overview(token)
            return {"overview": overview, "trendAvailable": False}

    async def product_performance(self, token: str) -> dict[str, Any]:
        return await self.request("GET", "/api/v1/admin/reports/products", token)

    async def order_overview(self, token: str) -> dict[str, Any]:
        return await self.request("GET", "/api/v1/admin/reports/orders", token)

    async def inventory_lookup(
        self,
        token: str,
        *,
        query: str | None = None,
        sku: str | None = None,
        variant_id: str | None = None,
        limit: int = 20,
    ) -> list[dict[str, Any]]:
        params: dict[str, Any] = {"limit": limit}
        if query:
            params["query"] = query
        if sku:
            params["sku"] = sku
        if variant_id:
            params["variantId"] = variant_id
        return await self.request("GET", "/api/v1/admin/reports/inventory/lookup", token, params=params)

    async def best_sellers(
        self,
        token: str,
        *,
        from_date: date,
        to_date: date,
        limit: int = 10,
    ) -> dict[str, Any]:
        return await self.request(
            "GET",
            "/api/v1/admin/reports/products/best-sellers",
            token,
            params={"fromDate": from_date.isoformat(), "toDate": to_date.isoformat(), "limit": limit},
        )
