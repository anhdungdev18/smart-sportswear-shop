from typing import Any
from pydantic import BaseModel


class ChatRequest(BaseModel):
    sessionId: str
    userId: str | None = None
    message: str
    channel: str = "web"


class SessionState(BaseModel):
    sessionId: str


class ChatResponse(BaseModel):
    reply: str
    toolCalls: list[Any] = []
    suggestions: list[str] = []
    sessionState: SessionState


class HealthResponse(BaseModel):
    status: str
    service: str
