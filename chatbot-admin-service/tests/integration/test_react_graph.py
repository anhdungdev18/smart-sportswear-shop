from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any
import asyncio

from app.graph.admin_graph import run_admin_graph
from tests.helpers import make_token


@dataclass
class RecordingRegistry:
    calls: list[tuple[str, dict[str, Any]]] = field(default_factory=list)

    async def execute(self, name: str, token: str, args: dict[str, Any]):
        self.calls.append((name, args))
        result = {
            "get_inventory_risks": [
                {"risk": "STOCKOUT", "availableQuantity": 4, "suggestedQuantity": 30},
                {"risk": "OVERSTOCK", "availableQuantity": 80, "suggestedQuantity": 0},
            ],
            "get_forecast_quality": {"totalVariants": 120, "highQualityVariants": 100, "insufficientVariants": 5},
            "get_ai_data_freshness": {"dataSource": "DEMO", "stale": False},
            "get_urgent_replenishment_candidates": {
                "content": [{"sku": "SKU-1", "priority": "CRITICAL", "suggestedQuantity": 30}]
            },
            "search_product_inventory": [{"sku": "SKU-1", "availableQuantity": 7, "stockQuantity": 9, "reservedQuantity": 2}],
            "get_best_selling_products": {"fromDate": "2026-06-28", "toDate": "2026-07-27", "items": []},
        }[name]
        return result, "fake"


def test_react_graph_executes_multi_tool_readonly_workflow():
    registry = RecordingRegistry()

    state = asyncio.run(
        run_admin_graph(
            "react-s1",
            "Stockout risk nao va why?",
            make_token("ADMIN"),
            registry=registry,  # type: ignore[arg-type]
        )
    )

    assert [name for name, _ in registry.calls] == ["get_inventory_risks", "get_forecast_quality"]
    assert state["selected_tool"] == "get_inventory_risks"
    assert len(state["tool_calls"]) == 2
    assert state["partial"] is False
    assert state["token"] == "[REDACTED]"
    assert all(step.get("node") != "chain_of_thought" for step in state["react_steps"])
    assert "rows=2" in state["grounded_numbers"]


def test_react_graph_stays_single_tool_for_simple_phase5_cases():
    registry = RecordingRegistry()

    state = asyncio.run(
        run_admin_graph(
            "react-s2",
            "Ton kho co risk nao?",
            make_token("ADMIN"),
            registry=registry,  # type: ignore[arg-type]
        )
    )

    assert [name for name, _ in registry.calls] == ["get_inventory_risks"]
    assert len(state["tool_calls"]) == 1
    assert state["partial"] is False


def test_phase9_urgent_replenishment_uses_freshness_then_rank_then_quality():
    registry = RecordingRegistry()

    state = asyncio.run(
        run_admin_graph(
            "phase9-s1",
            "Top 5 SKU can nhap cap bach",
            make_token("ADMIN"),
            registry=registry,  # type: ignore[arg-type]
        )
    )

    assert [name for name, _ in registry.calls] == [
        "get_ai_data_freshness",
        "get_urgent_replenishment_candidates",
        "get_forecast_quality",
    ]
    assert state["partial"] is False
