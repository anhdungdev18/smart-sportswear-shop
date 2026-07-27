from __future__ import annotations

from fastapi import APIRouter, Header

from app.graph.admin_graph import run_admin_graph
from app.memory.session_store import save_run
from app.observability.trace_logger import get_logger, log_run
from app.schemas.chat import ChatRequest, ChatResponse, ReactTraceStep, ToolCallRecord

router = APIRouter()
logger = get_logger(__name__)


@router.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest, authorization: str | None = Header(default=None)) -> ChatResponse:
    token = request.accessToken
    if not token and authorization and authorization.lower().startswith("bearer "):
        token = authorization[7:]

    state = await run_admin_graph(request.sessionId, request.message, token)
    run = {
        "runId": state["run_id"],
        "sessionId": request.sessionId,
        "actorId": state["actor"].actor_id,
        "role": state["actor"].role,
        "intent": state["intent"],
        "tool": state["selected_tool"],
        "source": state["tool_source"],
        "toolCalls": len(state.get("tool_calls", [])),
        "partial": state.get("partial", False),
        "warnings": state["warnings"],
    }
    save_run(run)
    log_run(logger, run)

    return ChatResponse(
        reply=state["reply"],
        intent=state["intent"],
        toolCalls=[
            ToolCallRecord(
                tool=call["tool"],
                args=call["args"],
                result=call["result"],
                source=call["source"],
                reason=call.get("reason"),
            )
            for call in state.get("tool_calls", [])
        ],
        trace=[ReactTraceStep(**step) for step in state.get("react_steps", [])],
        partial=state.get("partial", False),
        warnings=state["warnings"],
        groundedNumbers=state["grounded_numbers"],
        runId=state["run_id"],
    )
