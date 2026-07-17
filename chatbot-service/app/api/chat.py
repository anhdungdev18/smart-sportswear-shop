from __future__ import annotations

import time

from fastapi import APIRouter
from app.schemas.chat import ChatRequest, ChatResponse, SessionState, ToolCallRecord
from app.graph.chat_graph import run_chat_graph
from app.memory import session_store
from app.observability.trace_logger import get_logger, log_chat_request, log_pending_event
from app.observability import tool_call_logger, evaluation_logger

router = APIRouter()
logger = get_logger(__name__)


async def _update_normal_session(session_id: str, intent: str, result: dict) -> None:
    """Persist product context to session after a normal (non-blocked) turn."""
    ctx_update: dict = {"last_intent": intent}

    if intent == "PRODUCT_SEARCH" and result:
        items = result.get("items") or []
        if items:
            ctx_update.update({
                "last_product_ids": [it["productId"] for it in items],
                "last_products_summary": [
                    {"id": it["productId"], "name": it["name"], "slug": it["slug"]}
                    for it in items
                ],
                "selected_product_id": items[0]["productId"],
                "selected_product_name": items[0]["name"],
                "selected_variant_hints": {},
            })

    elif intent == "SKU_LOOKUP" and result:
        items = result.get("items") or []
        if items:
            first = items[0]
            ctx_update.update({
                "last_product_ids": list(dict.fromkeys(it["productId"] for it in items)),
                "last_products_summary": [
                    {"id": first["productId"], "name": first["name"], "slug": first["slug"]}
                ],
                "selected_product_id": first["productId"],
                "selected_product_name": first["name"],
                "selected_variant_hints": {
                    "variant_id": first["variantId"],
                    "sku": first["sku"],
                    "size": first.get("size"),
                    "color": first.get("color"),
                },
            })

    elif intent == "PRODUCT_DETAIL" and result.get("found"):
        hints = {
            k: v for k, v in {
                "size": result.get("sizeHint"),
                "color": result.get("colorHint"),
            }.items() if v
        }
        ctx_update.update({
            "selected_product_id": result.get("productId"),
            "selected_product_name": result.get("name"),
            "selected_variant_hints": hints,
        })

    await session_store.update_context(session_id, **ctx_update)


@router.post("/chat", response_model=ChatResponse)
async def chat(request: ChatRequest) -> ChatResponse:
    t_start = time.monotonic()

    log_chat_request(
        logger,
        session_id=request.sessionId,
        user_id=request.userId,
        channel=request.channel,
        message=request.message,
    )

    # Load session context (includes pending_action fields from Phase 9)
    try:
        session_context = await session_store.get_context(request.sessionId)
    except Exception as _ctx_err:
        logger.warning(f"[{request.sessionId}] session_get failed — {_ctx_err!r}")
        from app.memory.base_store import default_context
        session_context = default_context()

    # chat_history cold-start load is now handled inside the graph by load_context_node

    state = await run_chat_graph(
        session_id=request.sessionId,
        user_id=request.userId,
        channel=request.channel,
        message=request.message,
        user_role=request.userRole,
        access_token=request.accessToken,
        session_context=session_context,
    )

    intent      = state["intent"]
    result      = state.get("tool_result") or {}
    blocked     = state.get("execution_blocked", False)
    tool_name   = state["selected_tool"]
    latency_ms  = (time.monotonic() - t_start) * 1000

    # ── Phase 9: session update based on turn outcome ──────────────────────

    pending_event = ""

    try:
        if intent in ("CONFIRM_ACTION", "REJECT_ACTION", "EXPIRED_CONFIRMATION"):
            # Always clear pending action after the user responded (success or not)
            pending_event = "confirmed" if intent == "CONFIRM_ACTION" else (
                "rejected" if intent == "REJECT_ACTION" else "expired"
            )
            log_pending_event(logger, request.sessionId, event=pending_event,
                              tool=session_context.get("pending_action") or "")
            await session_store.clear_pending(request.sessionId)
            await session_store.update_context(request.sessionId, last_intent=intent)

        elif blocked and state.get("requires_confirmation"):
            # Save pending action so the next turn can confirm or reject
            pending_payload = {
                "tool": tool_name,
                "args": {
                    k: v for k, v in state.get("tool_args", {}).items()
                    if k != "access_token"   # never store token in session
                },
                "display": state.get("pending_action_display") or "thao tác này",
            }
            pending_event = "created"
            log_pending_event(logger, request.sessionId, event="created",
                              tool=tool_name, display=pending_payload["display"])
            await session_store.update_context(
                request.sessionId,
                last_intent=intent,
                pending_action=tool_name,
                pending_action_payload=pending_payload,
                pending_action_created_at=session_store.now_iso(),
            )

        elif not blocked:
            await _update_normal_session(request.sessionId, intent, result)

    except Exception as _sess_err:
        logger.warning(f"[{request.sessionId}] session_update failed — {_sess_err!r}")

    # chat_history is now saved inside the graph by save_result_node

    # ── Tool-call log ──────────────────────────────────────────────────────
    if tool_name != "none":
        tool_call_logger.log_tool_call(
            session_id=request.sessionId,
            tool_name=tool_name,
            args=state.get("tool_args", {}),
            result=result if result else None,
            latency_ms=latency_ms,
            success=not blocked and bool(result),
        )

    # ── Evaluation log ────────────────────────────────────────────────────
    evaluation_logger.log_turn(
        session_id=request.sessionId,
        user_id=request.userId,
        query=request.message,
        intent=intent,
        tool=tool_name,
        latency_ms=latency_ms,
        success=not blocked,
        reply=state["reply"],
        pending_event=pending_event,
    )

    # ── Build response ────────────────────────────────────────────────────
    tool_calls: list[ToolCallRecord] = []
    if tool_name != "none" and result and not blocked:
        tool_calls = [ToolCallRecord(tool=tool_name, result=result)]

    blocked_reason = state["policy_reason"] if blocked else None
    awaiting_confirmation = blocked and state.get("requires_confirmation", False)

    return ChatResponse(
        reply=state["reply"],
        toolCalls=tool_calls,
        suggestions=[],
        sessionState=SessionState(
            sessionId=request.sessionId,
            intent=intent,
            selectedTool=tool_name,
            blockedReason=blocked_reason,
            awaitingConfirmation=awaiting_confirmation,
            pendingAction=tool_name if awaiting_confirmation else None,
        ),
    )
