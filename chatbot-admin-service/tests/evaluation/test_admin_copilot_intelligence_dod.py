from __future__ import annotations

import asyncio
from dataclasses import dataclass, field
from typing import Any

import pytest

from app.graph.admin_graph import run_admin_graph
from app.graph.nodes.validate_answer import validate_answer
from app.policy import limits
from tests.helpers import make_token


@dataclass
class IntelligenceRegistry:
    calls: list[tuple[str, dict[str, Any]]] = field(default_factory=list)

    async def execute(self, name: str, token: str, args: dict[str, Any]):
        self.calls.append((name, args))
        result = {
            "get_order_overview": {"totalOrders": 42, "pendingOrders": 5},
            "get_order_status_trend": {"trendAvailable": False},
            "get_sales_overview": {"grossRevenue": 1000000, "realizedRevenue": 1500000},
            "get_revenue_breakdown": {
                "grossRevenue": 1000000,
                "realizedRevenue": 1500000,
                "difference": 500000,
                "codDeliveredUnpaid": {"orders": 2, "amount": 300000},
                "paidNotDelivered": {"orders": 1, "amount": 100000},
                "breakdownAvailable": True,
            },
            "get_inventory_risks": [{"sku": "SKU-1", "risk": "STOCKOUT", "availableQuantity": 4, "suggestedQuantity": 30}],
            "get_inventory_risk_explanation": {
                "sku": "SKU-1",
                "detail": {"sku": "SKU-1", "risk": "STOCKOUT", "availableQuantity": 4, "suggestedQuantity": 30},
                "lookup": [{"sku": "SKU-1", "stockQuantity": 5, "reservedQuantity": 1, "availableQuantity": 4}],
                "matchingRisks": [{"sku": "SKU-1", "risk": "STOCKOUT", "availableQuantity": 4, "suggestedQuantity": 30}],
                "forecastQuality": {"totalVariants": 120, "insufficientVariants": 5},
                "evidenceAvailable": True,
            },
            "get_ai_data_freshness": {"dataSource": "DEMO", "stale": False},
        }[name]
        return result, "fake"


def _run(session_id: str, message: str, registry: IntelligenceRegistry):
    limits._BUCKETS.clear()
    return asyncio.run(run_admin_graph(session_id, message, make_token("ADMIN"), registry=registry))  # type: ignore[arg-type]


@pytest.mark.parametrize(
    ("message", "expected_tool", "question_type"),
    [
        ("hien tai co bao nhieu don hang", "get_order_overview", "METRIC"),
        ("tai sao doanh thu ghi nhan va thuc nhan chenh nhau", "get_revenue_breakdown", "EXPLANATION"),
        ("Tai sao SKU-1 bi canh bao stockout?", "get_inventory_risk_explanation", "EXPLANATION"),
        ("don hang hom nay co bat thuong khong", "get_order_overview", "DIAGNOSIS"),
    ],
)
def test_intelligence_dod_routes_question_types_and_tools(message, expected_tool, question_type):
    registry = IntelligenceRegistry()

    state = _run(f"dod-{expected_tool}", message, registry)

    assert state["question_type"] == question_type
    assert state["selected_tool"] == expected_tool
    assert any(call[0] == expected_tool for call in registry.calls)
    assert state["grounded_numbers"]
    validate_answer(state["reply"], state["grounded_numbers"], question_type=state["question_type"])


def test_intelligence_dod_follow_up_uses_previous_revenue_context():
    token = make_token("ADMIN")
    limits._BUCKETS.clear()
    asyncio.run(run_admin_graph("dod-follow-up", "Doanh thu hien tai bao nhieu?", token, registry=IntelligenceRegistry()))  # type: ignore[arg-type]

    registry = IntelligenceRegistry()
    limits._BUCKETS.clear()
    state = asyncio.run(run_admin_graph("dod-follow-up", "Tai sao lai chenh vay?", token, registry=registry))  # type: ignore[arg-type]

    assert state["intent"] == "SALES_OVERVIEW"
    assert state["selected_tool"] == "get_revenue_breakdown"
    assert "500.000" in state["reply"]


def test_intelligence_dod_write_action_stays_approval_or_blocked():
    registry = IntelligenceRegistry()

    state = _run("dod-write", "Chay lai forecast ngay bay gio", registry)

    assert state["question_type"] == "ACTION_REQUEST"
    assert [name for name, _ in registry.calls] == ["get_ai_data_freshness"]
    assert not any(name.startswith(("sync_", "run_")) for name, _ in registry.calls)
