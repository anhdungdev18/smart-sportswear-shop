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
