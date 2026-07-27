from __future__ import annotations

from typing import Any
from urllib.parse import urljoin

import httpx

from app.config.settings import settings


class ApiClient:
    def __init__(self, base_url: str):
        self.base_url = base_url.rstrip("/") + "/"

    async def request(
        self,
        method: str,
        path: str,
        token: str,
        *,
        params: dict[str, Any] | None = None,
        json: dict[str, Any] | None = None,
    ) -> Any:
        url = urljoin(self.base_url, path.lstrip("/"))
        try:
            async with httpx.AsyncClient(timeout=settings.TOOL_TIMEOUT_SECONDS) as client:
                response = await client.request(
                    method,
                    url,
                    params=params,
                    json=json,
                    headers={"Authorization": f"Bearer {token}"},
                )
                response.raise_for_status()
                payload = response.json()
                return payload.get("data", payload)
        except httpx.TimeoutException as exc:
            raise TimeoutError(f"Tool request timed out: {method} {path}") from exc
