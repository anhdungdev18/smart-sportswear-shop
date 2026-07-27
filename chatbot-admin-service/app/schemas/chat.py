from __future__ import annotations

from typing import Any, Literal

from pydantic import BaseModel, Field


Intent = Literal[
    "INVENTORY_RISK",
    "REPLENISHMENT_EXPLANATION",
    "FORECAST_QUALITY",
    "SALES_OVERVIEW",
    "PRODUCT_PERFORMANCE",
    "ORDER_OVERVIEW",
    "WHAT_IF_SIMULATION",
    "UNKNOWN",
]


class ChatRequest(BaseModel):
    sessionId: str = Field(min_length=1, max_length=120)
    message: str = Field(min_length=1)
    accessToken: str | None = None


class ToolCallRecord(BaseModel):
    tool: str
    args: dict[str, Any] = Field(default_factory=dict)
    result: dict[str, Any] | list[Any] | None = None
    source: str
    reason: str | None = None


class ReactTraceStep(BaseModel):
    step: int
    node: str
    tool: str | None = None
    reason: str | None = None
    observation: str | None = None
    decision: str | None = None


class ChatResponse(BaseModel):
    reply: str
    intent: Intent
    toolCalls: list[ToolCallRecord] = Field(default_factory=list)
    trace: list[ReactTraceStep] = Field(default_factory=list)
    partial: bool = False
    warnings: list[str] = Field(default_factory=list)
    groundedNumbers: list[str] = Field(default_factory=list)
    runId: str
