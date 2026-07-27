from __future__ import annotations

import asyncio
import json
import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

from fastapi import HTTPException

from app.config.settings import settings
from app.graph.admin_graph import run_admin_graph
from app.graph.routing import classify_intent, select_tool
from app.policy import limits
from tests.helpers import make_token

READINESS_THRESHOLDS = {
    "toolSelectionAccuracy": 0.90,
    "groundedNumericAccuracy": 0.95,
    "readOnlyTaskSuccess": 0.85,
    "maxToolCallsPerRun": 1,
}


@dataclass
class EvaluatedCall:
    name: str
    args: dict[str, Any]


@dataclass
class EvaluationRegistry:
    calls: list[EvaluatedCall] = field(default_factory=list)

    async def execute(self, name: str, token: str, args: dict[str, Any]):
        self.calls.append(EvaluatedCall(name=name, args=args))
        result = {
            "get_inventory_risks": [
                {"risk": "STOCKOUT", "availableQuantity": 10, "suggestedQuantity": 68},
                {"risk": "OVERSTOCK", "availableQuantity": 220, "suggestedQuantity": 0},
            ],
            "get_replenishment_suggestions": {
                "content": [{"id": "r1", "status": "PENDING", "suggestedQuantity": 30}],
                "totalElements": 1,
            },
            "get_forecast_quality": {"totalVariants": 120, "highQualityVariants": 100, "insufficientVariants": 5},
            "get_sales_overview": {"totalOrders": 10, "revenue": 1000},
            "get_product_performance": {"items": [{"sku": "SKU-1", "sold": 12, "revenue": 300}]},
            "get_order_overview": {"totalOrders": 10, "pendingOrders": 2},
            "simulate_inventory_policy": {
                "currentDecision": {"suggestedQuantity": 10},
                "simulatedDecision": {"suggestedQuantity": 20},
            },
            "get_data_quality_summary": {"totalVariants": 120, "highQualityVariants": 100},
        }[name]
        return result, "fake"


def _load_cases(root: Path) -> list[dict[str, str]]:
    return json.loads((root / "evaluations" / "phase5_mvp_cases.json").read_text(encoding="utf-8"))


def _numbers_from_grounding(grounded_numbers: list[str]) -> set[str]:
    values: set[str] = set()
    for item in grounded_numbers:
        if "=" in item:
            values.add(item.rsplit("=", 1)[1])
    return values


def _reply_numbers_are_grounded(reply: str, grounded_numbers: list[str]) -> bool:
    reply_numbers = set(re.findall(r"(?<![\w-])\d+(?:\.\d+)?(?![\w-])", reply))
    if not reply_numbers:
        return bool(grounded_numbers)
    return reply_numbers.issubset(_numbers_from_grounding(grounded_numbers))


async def evaluate_phase7_readiness(root: Path | None = None) -> dict[str, Any]:
    project_root = root or Path.cwd()
    cases = _load_cases(project_root)
    admin_token = make_token("ADMIN")
    limits._BUCKETS.clear()

    route_correct = 0
    numeric_grounded = 0
    task_success = 0
    max_tool_calls_observed = 0
    failures: list[dict[str, str]] = []

    for case in cases:
        intent = classify_intent(case["query"])
        tool = select_tool(intent)
        if intent == case["expectedIntent"] and tool == case["expectedTool"]:
            route_correct += 1

        registry = EvaluationRegistry()
        try:
            state = await run_admin_graph(case["id"], case["query"], admin_token, registry=registry)  # type: ignore[arg-type]
        except Exception as exc:
            failures.append({"id": case["id"], "error": str(exc)})
            continue

        max_tool_calls_observed = max(max_tool_calls_observed, len(registry.calls))
        is_route_ok = state["intent"] == case["expectedIntent"] and state["selected_tool"] == case["expectedTool"]
        is_numeric_ok = bool(state["grounded_numbers"]) and _reply_numbers_are_grounded(state["reply"], state["grounded_numbers"])
        is_task_ok = (
            is_route_ok
            and is_numeric_ok
            and state["token"] == "[REDACTED]"
            and len(registry.calls) == 1
            and registry.calls[0].name == case["expectedTool"]
        )

        if is_numeric_ok:
            numeric_grounded += 1
        if is_task_ok:
            task_success += 1
        else:
            failures.append({"id": case["id"], "error": "task_success_gate_failed"})

    role_bypass_blocked = False
    role_registry = EvaluationRegistry()
    limits._BUCKETS.clear()
    try:
        await run_admin_graph("role-bypass", "Ton kho co risk nao?", make_token("WAREHOUSE_STAFF"), registry=role_registry)  # type: ignore[arg-type]
    except HTTPException as exc:
        role_bypass_blocked = exc.status_code == 403 and len(role_registry.calls) == 0
    finally:
        limits._BUCKETS.clear()

    total = len(cases)
    metrics = {
        "caseCount": total,
        "toolSelectionAccuracy": route_correct / total,
        "groundedNumericAccuracy": numeric_grounded / total,
        "readOnlyTaskSuccess": task_success / total,
        "roleBypassBlocked": role_bypass_blocked,
        "maxToolCallsObserved": max_tool_calls_observed,
        "infiniteLoopDetected": max_tool_calls_observed > settings.MAX_TOOL_CALLS_PER_RUN,
        "maxAllowedToolCallsPerRun": READINESS_THRESHOLDS["maxToolCallsPerRun"],
    }
    passed = (
        metrics["toolSelectionAccuracy"] >= READINESS_THRESHOLDS["toolSelectionAccuracy"]
        and metrics["groundedNumericAccuracy"] >= READINESS_THRESHOLDS["groundedNumericAccuracy"]
        and metrics["readOnlyTaskSuccess"] >= READINESS_THRESHOLDS["readOnlyTaskSuccess"]
        and metrics["roleBypassBlocked"]
        and metrics["maxToolCallsObserved"] <= READINESS_THRESHOLDS["maxToolCallsPerRun"]
        and not metrics["infiniteLoopDetected"]
    )

    return {
        "phase": "Phase 7 readiness",
        "status": "PASS" if passed else "FAIL",
        "thresholds": READINESS_THRESHOLDS,
        "metrics": metrics,
        "failures": failures[:20],
    }


def main() -> None:
    report = asyncio.run(evaluate_phase7_readiness(Path.cwd()))
    output_path = Path("evaluations") / "phase7_readiness_report.json"
    output_path.write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2, ensure_ascii=False))
    if report["status"] != "PASS":
        raise SystemExit(1)


if __name__ == "__main__":
    main()
