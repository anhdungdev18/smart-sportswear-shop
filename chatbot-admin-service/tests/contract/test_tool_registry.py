import asyncio
import pytest

from app.tools.registry import ToolRegistry


class FakeForecastingClient:
    async def data_quality_summary(self, token):
        return {"totalVariants": 120, "highQualityVariants": 100}

    async def inventory_risks(self, token, risk=None):
        return [{"variantId": "v1", "risk": "STOCKOUT"}]

    async def inventory_risk_detail(self, token, variant_id):
        return {"variantId": variant_id, "risk": "STOCKOUT"}

    async def replenishment_suggestions(self, token, **kwargs):
        return {"content": [{"id": "r1", "status": "PENDING"}], "totalElements": 1}

    async def replenishment_detail(self, token, recommendation_id):
        return {"id": recommendation_id}

    async def replenishment_explanation(self, token, recommendation_id):
        return {"recommendationId": recommendation_id, "decision": {"risk": "STOCKOUT"}}

    async def simulate_inventory_policy(self, token, payload):
        return {"currentDecision": {}, "simulatedDecision": {}}

    async def freshness(self, token, data_source=None):
        return {"dataSource": data_source or "DEMO", "stale": False}

    async def job_status(self, token, job_id):
        return {"jobId": job_id, "status": "IDLE"}


class FakeBackendClient:
    async def sales_overview(self, token):
        return {"orders": 10}

    async def product_performance(self, token):
        return {"items": []}

    async def order_overview(self, token):
        return {"totalOrders": 10}

    async def inventory_lookup(self, token, **kwargs):
        return [{"sku": "SKU-1", "availableQuantity": 7}]

    async def best_sellers(self, token, **kwargs):
        return {"items": [{"productName": "Ao chay bo", "unitsSold": 12}], "source": "order_items_excluding_cancelled"}


def test_registry_executes_read_only_forecasting_tool():
    registry = ToolRegistry(forecasting=FakeForecastingClient(), backend=FakeBackendClient())

    result, source = asyncio.run(registry.execute("get_replenishment_suggestions", "token", {"status": "PENDING"}))

    assert source == "forecasting"
    assert result["totalElements"] == 1


def test_registry_rejects_non_allowlisted_tool():
    registry = ToolRegistry(forecasting=FakeForecastingClient(), backend=FakeBackendClient())

    with pytest.raises(PermissionError):
        asyncio.run(registry.execute("accept_replenishment", "token", {}))


def test_registry_executes_phase9_read_only_tools():
    registry = ToolRegistry(forecasting=FakeForecastingClient(), backend=FakeBackendClient())

    lookup, lookup_source = asyncio.run(registry.execute("search_product_inventory", "token", {"query": "SKU-1"}))
    freshness, freshness_source = asyncio.run(registry.execute("get_ai_data_freshness", "token", {"dataSource": "DEMO"}))

    assert lookup_source == "backend"
    assert lookup[0]["availableQuantity"] == 7
    assert freshness_source == "forecasting"
    assert freshness["stale"] is False


def test_registry_blocks_controlled_ai_jobs_by_default():
    registry = ToolRegistry(forecasting=FakeForecastingClient(), backend=FakeBackendClient())

    with pytest.raises(PermissionError):
        asyncio.run(registry.execute("run_forecast_generation", "token", {"dataSource": "DEMO"}))
