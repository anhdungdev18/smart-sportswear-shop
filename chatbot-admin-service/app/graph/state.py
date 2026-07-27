from __future__ import annotations

from typing import Any, TypedDict

from app.auth.actor_context import ActorContext
from app.schemas.chat import Intent


class ToolCallState(TypedDict, total=False):
    tool: str
    args: dict[str, Any]
    result: Any
    source: str
    reason: str


class ReactStepState(TypedDict, total=False):
    step: int
    node: str
    tool: str
    reason: str
    observation: str
    decision: str


class AdminGraphState(TypedDict, total=False):
    session_id: str
    message: str
    token: str
    actor: ActorContext
    intent: Intent
    selected_tool: str
    tool_args: dict[str, Any]
    tool_result: Any
    tool_source: str
    tool_calls: list[ToolCallState]
    react_steps: list[ReactStepState]
    partial: bool
    reply: str
    warnings: list[str]
    grounded_numbers: list[str]
    run_id: str
