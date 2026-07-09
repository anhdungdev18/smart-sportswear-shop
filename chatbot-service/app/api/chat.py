from __future__ import annotations

from fastapi import APIRouter
from app.schemas.chat import ChatRequest, ChatResponse, SessionState, ToolCallRecord
from app.graph.chat_graph import run_chat_graph
from app.observability.trace_logger import get_logger, log_chat_request

router = APIRouter()
logger = get_logger(__name__)


@router.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest) -> ChatResponse:
    log_chat_request(
        logger,
        session_id=request.sessionId,
        user_id=request.userId,
        channel=request.channel,
        message=request.message,
    )

    state = await run_chat_graph(
        session_id=request.sessionId,
        user_id=request.userId,
        channel=request.channel,
        message=request.message,
    )

    tool_calls: list[ToolCallRecord] = []
    if state["selected_tool"] != "none" and state["tool_result"]:
        tool_calls = [ToolCallRecord(tool=state["selected_tool"], result=state["tool_result"])]

    return ChatResponse(
        reply=state["reply"],
        toolCalls=tool_calls,
        suggestions=[],
        sessionState=SessionState(
            sessionId=request.sessionId,
            intent=state["intent"],
            selectedTool=state["selected_tool"],
        ),
    )
