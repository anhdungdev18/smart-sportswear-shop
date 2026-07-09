from __future__ import annotations

from typing import Any
from pydantic import BaseModel


class ChatRequest(BaseModel):
    sessionId: str
    userId: str | None = None
    message: str
    channel: str = "web"


class ToolCallRecord(BaseModel):
    tool: str
    result: dict[str, Any]


class SessionState(BaseModel):
    sessionId: str
    intent: str | None = None
    selectedTool: str | None = None


class ChatResponse(BaseModel):
    reply: str
    toolCalls: list[ToolCallRecord] = []
    suggestions: list[str] = []
    sessionState: SessionState


class HealthResponse(BaseModel):
    status: str
    service: str
