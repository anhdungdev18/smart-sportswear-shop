from __future__ import annotations

from typing import Any
from pydantic import BaseModel


class ChatRequest(BaseModel):
    sessionId: str
    userId: str | None = None
    userRole: str | None = None         # Phase 2: "guest" | "customer" | "admin"
    accessToken: str | None = None      # Phase 5: Bearer token forwarded to backend
    message: str
    channel: str = "web"


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
