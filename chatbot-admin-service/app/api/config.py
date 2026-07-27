from __future__ import annotations

from datetime import datetime, timezone

from fastapi import APIRouter
from pydantic import BaseModel, Field

from app.config.settings import settings
from app.memory.session_store import save_feedback
from app.policy.capability_policy import READ_ONLY_TOOLS

router = APIRouter(prefix="/config")


class FeedbackRequest(BaseModel):
    runId: str = Field(min_length=1, max_length=120)
    rating: str = Field(pattern="^(CORRECT|INCORRECT)$")
    note: str | None = Field(default=None, max_length=1000)


@router.get("")
async def config() -> dict:
    return {
        "environment": settings.ADMIN_COPILOT_ENV,
        "modelProvider": settings.MODEL_PROVIDER,
        "modelName": settings.MODEL_NAME,
        "promptVersion": "admin-copilot-react-readonly-v1",
        "maxAgentSteps": settings.MAX_AGENT_STEPS,
        "maxToolCallsPerRun": settings.MAX_TOOL_CALLS_PER_RUN,
        "agentTimeoutSeconds": settings.AGENT_TIMEOUT_SECONDS,
        "toolTimeoutSeconds": settings.TOOL_TIMEOUT_SECONDS,
        "maxInputChars": settings.MAX_INPUT_CHARS,
        "readOnlyMode": settings.READ_ONLY_MODE,
        "writeToolsEnabled": settings.WRITE_TOOLS_ENABLED,
        "approvalsEnabled": settings.APPROVALS_ENABLED,
        "observabilityEnabled": settings.OBSERVABILITY_ENABLED,
        "evaluationLoggingEnabled": settings.EVALUATION_LOGGING_ENABLED,
        "rateLimitPerMinute": settings.RATE_LIMIT_PER_MINUTE,
        "enabledTools": sorted(READ_ONLY_TOOLS),
        "approvalWriteActions": [
            "ACCEPT_REPLENISHMENT",
            "ADJUST_REPLENISHMENT",
            "DISMISS_REPLENISHMENT",
        ],
        "rolePermissions": [
            {"role": "ADMIN", "access": "FULL_READ_ONLY"},
            {"role": "SALES", "access": "BLOCKED_IN_PHASE_6"},
            {"role": "WAREHOUSE", "access": "BLOCKED_IN_PHASE_6"},
        ],
        "evaluation": {
            "dataset": "phase5_mvp_cases.json",
            "cases": 50,
            "lastResult": "passed",
            "phase7ReadinessReport": "phase7_readiness_report.json",
        },
        "cost": {
            "tokenCount": 0,
            "estimatedCost": 0,
            "currency": "USD",
            "note": "Current Copilot answers and approval workflow are deterministic; no LLM token spend is recorded.",
        },
        "updatedAt": datetime.now(timezone.utc).isoformat(),
    }


@router.post("/feedback")
async def feedback(request: FeedbackRequest) -> dict:
    save_feedback(
        {
            "runId": request.runId,
            "rating": request.rating,
            "note": request.note,
            "createdAt": datetime.now(timezone.utc).isoformat(),
        }
    )
    return {"saved": True}
