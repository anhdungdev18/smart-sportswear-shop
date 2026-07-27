from __future__ import annotations

from uuid import uuid4

from fastapi import HTTPException, status

from app.auth.jwt_verifier import verify_admin_jwt
from app.graph.nodes.generate_answer import generate_grounded_answer
from app.graph.nodes.input_guard import guard_input
from app.graph.nodes.policy_guard import guard_policy
from app.graph.nodes.validate_answer import validate_answer
from app.graph.routing import classify_intent, select_tool
from app.graph.state import AdminGraphState
from app.tools.registry import ToolRegistry


async def run_admin_graph(session_id: str, message: str, token: str | None, registry: ToolRegistry | None = None) -> AdminGraphState:
    run_id = str(uuid4())
    actor = verify_admin_jwt(token)
    guard_input(message)

    intent = classify_intent(message)
    tool_name = select_tool(intent)
    guard_policy(actor, tool_name)

    tool_args = {"limit": 20}
    if tool_name == "simulate_inventory_policy":
        tool_args = {}

    tool_registry = registry or ToolRegistry()
    try:
        result, source = await tool_registry.execute(tool_name, token or "", tool_args)
    except TimeoutError as exc:
        raise HTTPException(
            status_code=status.HTTP_504_GATEWAY_TIMEOUT,
            detail="Admin Copilot tool timed out",
        ) from exc
    reply, warnings, numbers = generate_grounded_answer(intent, tool_name, result)
    validate_answer(reply, numbers)

    return AdminGraphState(
        session_id=session_id,
        message=message,
        token="[REDACTED]",
        actor=actor,
        intent=intent,
        selected_tool=tool_name,
        tool_args=tool_args,
        tool_result=result,
        tool_source=source,
        reply=reply,
        warnings=warnings,
        grounded_numbers=numbers,
        run_id=run_id,
    )
