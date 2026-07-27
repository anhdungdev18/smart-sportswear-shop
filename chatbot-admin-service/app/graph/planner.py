from __future__ import annotations

from dataclasses import dataclass
from datetime import date, timedelta
import re
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

    if intent == "URGENT_REPLENISHMENT_ANALYSIS":
        plan = [
            PlannedToolCall("get_ai_data_freshness", {"dataSource": "DEMO"}, "freshness gate for urgent replenishment"),
            PlannedToolCall("get_urgent_replenishment_candidates", {"limit": _extract_limit(text, 5)}, "rank pending replenishment"),
            PlannedToolCall("get_forecast_quality", {}, "quality context for replenishment ranking"),
        ]
    elif intent == "AI_PIPELINE_REFRESH":
        plan = [PlannedToolCall("get_ai_data_freshness", {"dataSource": "DEMO"}, "freshness check before controlled jobs")]
        if settings.CONTROLLED_AI_JOBS_ENABLED:
            correlation_id = f"phase9-{date.today().isoformat()}"
            plan.extend([
                PlannedToolCall("sync_ai_snapshot", {"dataSource": "DEMO", "correlationId": correlation_id}, "controlled snapshot sync"),
                PlannedToolCall("run_demand_classification", {"dataSource": "DEMO", "correlationId": correlation_id}, "controlled demand classification"),
                PlannedToolCall("run_forecast_evaluation", {"dataSource": "DEMO", "correlationId": correlation_id}, "controlled evaluation"),
                PlannedToolCall("run_forecast_generation", {"dataSource": "DEMO", "correlationId": correlation_id}, "controlled forecast generation"),
                PlannedToolCall("get_ai_job_status", {"jobId": correlation_id}, "poll current AI batch status"),
            ])
    elif intent == "BEST_SELLING_PRODUCTS":
        to_date = date.today()
        lookback_days = _extract_lookback_days(text)
        from_date = to_date - timedelta(days=lookback_days - 1)
        plan = [
            PlannedToolCall(
                "get_best_selling_products",
                {"fromDate": from_date.isoformat(), "toDate": to_date.isoformat(), "limit": _extract_limit(text, 10)},
                "best sellers for explicit date range",
            )
        ]
    elif intent == "PRODUCT_INVENTORY_LOOKUP":
        plan = [
            PlannedToolCall(
                "search_product_inventory",
                {"query": _extract_lookup_query(message), "limit": _extract_limit(text, 20)},
                "deterministic product inventory lookup",
            )
        ]

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


def _extract_limit(text: str, default: int) -> int:
    match = re.search(r"\btop\s+(\d+)|\b(\d+)\s+(?:sku|san|mon)", text)
    if not match:
        return min(default, settings.MAX_AGENT_RESULT_ROWS)
    value = int(next(group for group in match.groups() if group))
    return max(1, min(value, settings.MAX_AGENT_RESULT_ROWS))


def _extract_lookback_days(text: str) -> int:
    if "7 ng" in text or "1 tuan" in text:
        return 7
    if "thang nay" in text or "1 thang" in text or "1 thÃ¡ng" in text:
        return min(30, settings.MAX_REPORT_LOOKBACK_DAYS)
    match = re.search(r"(\d+)\s+ng", text)
    if match:
        return max(1, min(int(match.group(1)), settings.MAX_REPORT_LOOKBACK_DAYS))
    return min(settings.DEFAULT_REPORT_LOOKBACK_DAYS, settings.MAX_REPORT_LOOKBACK_DAYS)


def _extract_lookup_query(message: str) -> str:
    cleaned = re.sub(r"\b(con bao nhieu|cÃ²n bao nhiÃªu|con ton|cÃ²n tá»“n|ton kho|tá»“n kho)\b", " ", message, flags=re.I)
    return " ".join(cleaned.split()).strip() or message.strip()
