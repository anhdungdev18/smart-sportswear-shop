import asyncio

import httpx
import pytest

from app.clients.base import ApiClient
from app.clients.forecasting_client import ForecastingClient


class FakeAsyncClient:
    calls = []

    def __init__(self, timeout):
        self.timeout = timeout

    async def __aenter__(self):
        return self

    async def __aexit__(self, exc_type, exc, tb):
        return False

    async def request(self, method, url, params=None, json=None, headers=None):
        self.calls.append(
            {
                "method": method,
                "url": url,
                "params": params,
                "json": json,
                "headers": headers,
                "timeout": self.timeout,
            }
        )
        return httpx.Response(200, json={"data": {"ok": True}}, request=httpx.Request(method, url))


class TimeoutAsyncClient(FakeAsyncClient):
    async def request(self, method, url, params=None, json=None, headers=None):
        raise httpx.ReadTimeout("timeout")


def test_api_client_uses_bearer_header_timeout_and_unwraps_data(monkeypatch):
    FakeAsyncClient.calls = []
    monkeypatch.setattr(httpx, "AsyncClient", FakeAsyncClient)

    result = asyncio.run(ApiClient("http://localhost:8081").request("GET", "/api/v1/test", "token-123"))

    assert result == {"ok": True}
    assert FakeAsyncClient.calls[0]["headers"]["Authorization"] == "Bearer token-123"
    assert FakeAsyncClient.calls[0]["timeout"] > 0


def test_api_client_maps_httpx_timeout(monkeypatch):
    monkeypatch.setattr(httpx, "AsyncClient", TimeoutAsyncClient)

    with pytest.raises(TimeoutError):
        asyncio.run(ApiClient("http://localhost:8081").request("GET", "/api/v1/test", "token-123"))


def test_forecasting_client_contract_for_pending_suggestions(monkeypatch):
    FakeAsyncClient.calls = []
    monkeypatch.setattr(httpx, "AsyncClient", FakeAsyncClient)

    asyncio.run(ForecastingClient().replenishment_suggestions("token-123", status="PENDING", limit=100))

    call = FakeAsyncClient.calls[0]
    assert call["method"] == "GET"
    assert call["url"].endswith("/api/v1/admin/replenishment/suggestions")
    assert call["params"]["status"] == "PENDING"
    assert call["params"]["limit"] == 100
