from __future__ import annotations

from typing import Any
from pydantic import BaseModel, Field


class ChatRequest(BaseModel):
    sessionId: str = Field(min_length=1, max_length=128)
    # Deprecated compatibility hints. Identity and role only come from the verified JWT.
    userId: str | None = None
    userRole: str | None = None
    accessToken: str | None = Field(default=None, max_length=4096)
    message: str = Field(min_length=1, max_length=4000)
    channel: str = Field(default="web", min_length=1, max_length=32)


class ToolCallRecord(BaseModel):
    tool: str
    result: dict[str, Any]


class SessionState(BaseModel):
    sessionId: str
    intent: str | None = None
    selectedTool: str | None = None
    blockedReason: str | None = None        # Phase 2
    awaitingConfirmation: bool = False      # Phase 9
    pendingAction: str | None = None        # Phase 9: tool name of pending action


class ChatResponse(BaseModel):
    reply: str
    toolCalls: list[ToolCallRecord] = []
    suggestions: list[str] = []
    sessionState: SessionState


class HealthResponse(BaseModel):
    status: str
    service: str
