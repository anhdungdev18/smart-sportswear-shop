from __future__ import annotations

from typing import Any, Literal

from pydantic import BaseModel, Field

ApprovalAction = Literal[
    "ACCEPT_REPLENISHMENT",
    "ADJUST_REPLENISHMENT",
    "DISMISS_REPLENISHMENT",
]
ApprovalStatus = Literal["PENDING", "APPROVED", "EXECUTED", "REJECTED", "EXPIRED", "FAILED"]
RiskLevel = Literal["LOW", "MEDIUM", "HIGH", "CRITICAL"]


class CreateApprovalRequest(BaseModel):
    action: ApprovalAction
    resourceType: str = Field(default="REPLENISHMENT_RECOMMENDATION", max_length=80)
    resourceId: str = Field(min_length=1, max_length=120)
    payload: dict[str, Any] = Field(default_factory=dict)
    idempotencyKey: str = Field(min_length=8, max_length=160)
    reason: str = Field(min_length=1, max_length=1000)
    riskLevel: RiskLevel = "MEDIUM"


class ApprovalDecisionRequest(BaseModel):
    note: str | None = Field(default=None, max_length=1000)


class ApprovalResponse(BaseModel):
    id: str
    action: ApprovalAction
    resourceType: str
    resourceId: str
    payload: dict[str, Any]
    payloadHash: str
    idempotencyKey: str
    reason: str
    riskLevel: RiskLevel
    status: ApprovalStatus
    requestedBy: str
    approvedBy: str | None = None
    executedBy: str | None = None
    beforeSnapshot: dict[str, Any] | None = None
    afterSnapshot: dict[str, Any] | None = None
    audit: list[dict[str, Any]] = Field(default_factory=list)
    error: str | None = None
    expiresAt: str
    createdAt: str
    updatedAt: str
