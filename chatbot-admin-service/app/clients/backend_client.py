from __future__ import annotations

from typing import Any

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
