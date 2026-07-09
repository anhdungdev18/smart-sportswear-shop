from fastapi import APIRouter
from app.schemas.chat import ChatRequest, ChatResponse, SessionState
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

    # TODO Phase 1: replace with graph invocation
    # result = await run_chat_graph(request.sessionId, request.userId, request.message)
    return ChatResponse(
        reply="Đây là phản hồi mock từ chatbot-service Phase 0.",
        toolCalls=[],
        suggestions=[],
        sessionState=SessionState(sessionId=request.sessionId),
    )
