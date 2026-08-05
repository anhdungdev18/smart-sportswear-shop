from __future__ import annotations

import asyncio
import json
import time

from fastapi import APIRouter
from fastapi.responses import StreamingResponse

from app.api.chat import apply_turn_session_updates
from app.graph.chat_graph import run_chat_graph
from app.graph.stream_context import stream_sink
from app.memory import session_store
from app.observability.trace_logger import get_logger, log_chat_request
from app.schemas.chat import ChatRequest
from app.services.auth_identity import session_storage_id, verify_access_token

router = APIRouter()
logger = get_logger(__name__)


def _sse(payload: dict) -> str:
    return f"data: {json.dumps(payload, ensure_ascii=False)}\n\n"


def _extract_products(state: dict) -> list[dict]:
    """Pull a compact, clickable product list out of the tool result so the
    widget can render image cards linking to each product detail page.

    Works for search / recommend / sku-lookup items — all expose slug, name,
    a price (priceMin or price) and primaryImage.
    """
    result = state.get("tool_result") or {}
    items = result.get("items") or []
    products: list[dict] = []
    for it in items[:6]:
        if not isinstance(it, dict):
            continue
        slug = it.get("slug")
        name = it.get("name")
        if not slug or not name:
            continue
        products.append({
            "name": name,
            "slug": slug,
            "price": it.get("priceMin") or it.get("price") or it.get("priceMax"),
            "image": it.get("primaryImage"),
        })
    return products


@router.post("/chat/stream")
async def chat_stream(request: ChatRequest) -> StreamingResponse:
    """Server-Sent Events variant of /chat.

    Runs the same graph but streams the final answer token-by-token, so the
    browser shows text within ~1s instead of waiting for the whole reply.
    """
    identity = verify_access_token(request.accessToken)
    verified_token = request.accessToken if identity is not None else None
    user_id = identity.user_id if identity is not None else None
    user_role = identity.role if identity is not None else None
    storage_session_id = session_storage_id(request.sessionId, identity)

    log_chat_request(
        logger, session_id=storage_session_id, user_id=user_id,
        channel=request.channel, message=request.message,
    )

    try:
        session_context = await session_store.get_context(storage_session_id)
    except Exception as exc:
        logger.warning(f"[{storage_session_id}] session_get failed — {exc!r}")
        from app.memory.base_store import default_context
        session_context = default_context()

    async def event_gen():
        t0 = time.monotonic()
        queue: asyncio.Queue = asyncio.Queue()
        token = stream_sink.set(queue)
        try:
            graph_task = asyncio.create_task(run_chat_graph(
                session_id=storage_session_id,
                user_id=user_id,
                channel=request.channel,
                message=request.message,
                user_role=user_role,
                access_token=verified_token,
                session_context=session_context,
                is_new_session=request.isNewSession,
            ))

            streamed_any = False
            while not (graph_task.done() and queue.empty()):
                try:
                    delta = await asyncio.wait_for(queue.get(), timeout=0.1)
                    streamed_any = True
                    yield _sse({"delta": delta})
                except asyncio.TimeoutError:
                    continue

            state = graph_task.result()
            reply = state.get("reply", "") or ""

            # Deterministic replies (auth/confirm/etc.) never stream — emit once.
            if not streamed_any and reply:
                yield _sse({"delta": reply})

            intent = state.get("intent") or "UNKNOWN"
            await apply_turn_session_updates(storage_session_id, intent, state, session_context)

            awaiting = state.get("execution_blocked", False) and state.get("requires_confirmation", False)
            yield _sse({
                "done": True,
                "reply": reply,
                "products": _extract_products(state),
                "sessionState": {
                    "sessionId": request.sessionId,
                    "intent": intent,
                    "awaitingConfirmation": awaiting,
                },
                "latencyMs": int((time.monotonic() - t0) * 1000),
            })
        except Exception as exc:
            logger.warning(f"[{storage_session_id}] chat_stream failed — {exc!r}")
            yield _sse({"error": "Xin lỗi, hiện mình chưa trả lời được. Bạn thử lại nhé."})
        finally:
            stream_sink.reset(token)

    return StreamingResponse(
        event_gen(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",
        },
    )
