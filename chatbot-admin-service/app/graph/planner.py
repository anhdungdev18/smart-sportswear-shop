from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Literal

from app.config.settings import settings
from app.graph.routing import select_tool
from app.schemas.chat import Intent

PlanStatus = Literal["CONTINUE", "FINAL_ANSWER", "LIMIT_REACHED"]


@dataclass(frozen=True)
class PlannedToolCall:
    tool: str
    args: dict[str, Any]
    reason: str


def build_readonly_plan(intent: Intent, message: str) -> list[PlannedToolCall]:
    text = message.lower()
    primary_tool = select_tool(intent)
    plan = [PlannedToolCall(primary_tool, _default_args(primary_tool), f"primary tool for {intent}")]

    needs_quality_context = any(
        term in text
        for term in [
            "why",
            "explain",
            "giải thích",
            "giai thich",
            "confidence",
            "quality",
            "chất lượng",
            "chat luong",
            "độ tin cậy",
            "do tin cay",
        ]
    )
    needs_replenishment_context = any(
        term in text
        for term in ["replenishment", "đề xuất nhập", "de xuat nhap", "nhập hàng", "nhap hang", "suggestion"]
    )

    if intent == "INVENTORY_RISK" and needs_quality_context:
        _append_unique(plan, "get_forecast_quality", {}, "quality context for inventory risk")
    if intent == "FORECAST_QUALITY" and any(term in text for term in ["stockout", "overstock", "risk", "rủi ro", "rui ro"]):
        _append_unique(plan, "get_inventory_risks", {"limit": 20}, "risk context for forecast quality")
    if intent == "REPLENISHMENT_EXPLANATION" and needs_quality_context:
        _append_unique(plan, "get_forecast_quality", {}, "quality context for replenishment explanation")
    if intent == "UNKNOWN" and needs_replenishment_context:
        _append_unique(plan, "get_replenishment_suggestions", {"status": "PENDING", "limit": 20}, "fallback replenishment context")

    max_calls = max(1, min(settings.MAX_TOOL_CALLS_PER_RUN, settings.MAX_AGENT_STEPS))
    return plan[:max_calls]


def decide_next(completed_calls: int, planned_calls: int, repeated_call_detected: bool) -> PlanStatus:
    if repeated_call_detected or completed_calls >= settings.MAX_TOOL_CALLS_PER_RUN or completed_calls >= settings.MAX_AGENT_STEPS:
        return "LIMIT_REACHED"
    if completed_calls >= planned_calls:
        return "FINAL_ANSWER"
    return "CONTINUE"


def _append_unique(plan: list[PlannedToolCall], tool: str, args: dict[str, Any], reason: str) -> None:
    if all(existing.tool != tool or existing.args != args for existing in plan):
        plan.append(PlannedToolCall(tool, args, reason))


def _default_args(tool: str) -> dict[str, Any]:
    if tool == "get_inventory_risks":
        return {"limit": 20}
    if tool == "get_replenishment_suggestions":
        return {"status": "PENDING", "limit": 20}
    if tool == "simulate_inventory_policy":
        return {}
    return {}
