import json
import asyncio
from pathlib import Path

from app.auth.jwt_verifier import verify_admin_jwt
from app.graph.admin_graph import run_admin_graph
from app.graph.routing import classify_intent, select_tool
from tests.helpers import make_token


def test_phase5_evaluation_set_has_50_cases_and_expected_routes():
    cases = json.loads(Path("evaluations/phase5_mvp_cases.json").read_text(encoding="utf-8"))

    assert len(cases) >= 50
    for case in cases:
        intent = classify_intent(case["query"])
        assert intent == case["expectedIntent"], case["id"]
        assert select_tool(intent) == case["expectedTool"], case["id"]


class FakeEvaluationRegistry:
    async def execute(self, name, token, args):
        result = {
            "get_inventory_risks": [{"risk": "STOCKOUT"}, {"risk": "OVERSTOCK"}],
            "get_replenishment_suggestions": {"content": [{"id": "r1", "status": "PENDING"}], "totalElements": 1},
            "get_forecast_quality": {"totalVariants": 120, "highQualityVariants": 100},
            "get_sales_overview": {"totalOrders": 10, "revenue": 1000},
            "get_product_performance": {"items": [{"sku": "SKU-1", "sold": 12}]},
            "get_order_overview": {"totalOrders": 10},
            "simulate_inventory_policy": {"currentDecision": {"suggestedQuantity": 10}, "simulatedDecision": {"suggestedQuantity": 20}},
            "get_data_quality_summary": {"totalVariants": 120},
        }[name]
        return result, "fake"


def test_phase5_evaluation_answers_are_grounded_for_all_cases():
    cases = json.loads(Path("evaluations/phase5_mvp_cases.json").read_text(encoding="utf-8"))
    token = make_token("ADMIN")
    verify_admin_jwt(token)

    for case in cases:
        state = asyncio.run(run_admin_graph(case["id"], case["query"], token, registry=FakeEvaluationRegistry()))
        assert state["intent"] == case["expectedIntent"], case["id"]
        assert state["selected_tool"] == case["expectedTool"], case["id"]
        assert state["grounded_numbers"], case["id"]
        assert state["token"] == "[REDACTED]", case["id"]
