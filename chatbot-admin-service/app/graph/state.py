from __future__ import annotations

from typing import Any, TypedDict

from app.auth.actor_context import ActorContext
from app.schemas.chat import Intent


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
    reply: str
    warnings: list[str]
    grounded_numbers: list[str]
    run_id: str
