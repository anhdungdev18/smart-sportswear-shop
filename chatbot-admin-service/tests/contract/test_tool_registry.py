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


class FakeBackendClient:
    async def sales_overview(self, token):
        return {"orders": 10}

    async def product_performance(self, token):
        return {"items": []}

    async def order_overview(self, token):
        return {"totalOrders": 10}


def test_registry_executes_read_only_forecasting_tool():
    registry = ToolRegistry(forecasting=FakeForecastingClient(), backend=FakeBackendClient())

    result, source = asyncio.run(registry.execute("get_replenishment_suggestions", "token", {"status": "PENDING"}))

    assert source == "forecasting"
    assert result["totalElements"] == 1


def test_registry_rejects_non_allowlisted_tool():
    registry = ToolRegistry(forecasting=FakeForecastingClient(), backend=FakeBackendClient())

    with pytest.raises(PermissionError):
        asyncio.run(registry.execute("accept_replenishment", "token", {}))
