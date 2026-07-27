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
    "PRODUCT_INVENTORY_LOOKUP",
    "BEST_SELLING_PRODUCTS",
    "URGENT_REPLENISHMENT_ANALYSIS",
    "AI_PIPELINE_REFRESH",
    "AI_DATA_FRESHNESS",
    "UNKNOWN",
]

QuestionType = Literal[
    "METRIC",
    "EXPLANATION",
    "COMPARISON",
    "DIAGNOSIS",
    "RECOMMENDATION",
    "DETAIL_LOOKUP",
    "ACTION_REQUEST",
    "FOLLOW_UP",
    "UNKNOWN",
]


class ClassificationResult(BaseModel):
    intent: Intent
    questionType: QuestionType = "UNKNOWN"
    neededTools: list[str] = Field(default_factory=list)
    entities: dict[str, Any] = Field(default_factory=dict)
    timeRange: dict[str, Any] | None = None
    confidence: float = Field(default=1.0, ge=0.0, le=1.0)
    clarifyingQuestion: str | None = None


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
    questionType: QuestionType = "UNKNOWN"
    toolCalls: list[ToolCallRecord] = Field(default_factory=list)
    trace: list[ReactTraceStep] = Field(default_factory=list)
    partial: bool = False
    warnings: list[str] = Field(default_factory=list)
    groundedNumbers: list[str] = Field(default_factory=list)
    runId: str
