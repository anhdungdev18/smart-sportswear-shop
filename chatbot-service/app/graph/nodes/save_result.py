"""
Save result node — last node in the graph, mirrors DigiAISaleAgent's save_result.

Persists the completed turn:
  1. Updates chat_history in the in-memory session store (keep last 10 messages)
  2. Fire-and-forget async save of user + assistant messages to the DB
"""
from __future__ import annotations

import asyncio

from app.graph.state import AgentState
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_MAX_HISTORY = 10  # messages = 5 turns
_pending_saves: set[asyncio.Task] = set()


def _schedule_save(coro) -> None:
    task = asyncio.create_task(coro)
    _pending_saves.add(task)
    task.add_done_callback(_pending_saves.discard)


async def drain_pending_saves() -> None:
    """Wait for tracked history writes during graceful application shutdown."""
    if _pending_saves:
        await asyncio.gather(*tuple(_pending_saves), return_exceptions=True)


async def save_result_node(state: AgentState) -> dict:
    session_id = state["session_id"]
    message    = state.get("message") or ""
    reply      = state.get("reply") or ""
    intent     = state.get("intent") or ""
    user_id    = state.get("user_id")

    # Build updated chat_history (pre-turn history is in initial state)
    current_history: list[dict] = state.get("chat_history") or []
    new_history = [
        *current_history,
        {"role": "user",      "content": message},
        {"role": "assistant", "content": reply},
    ]
    if len(new_history) > _MAX_HISTORY:
        new_history = new_history[-_MAX_HISTORY:]

    # Persist to in-memory session store
    try:
        from app.memory import session_store
        await session_store.update_context(session_id, chat_history=new_history)
    except Exception as exc:
        logger.warning(f"[{session_id}] save_result | session_error={exc!r}")

    # Fire-and-forget DB persist (never blocks the response)
    _schedule_save(_db_save(session_id, user_id, message, reply, intent))

    return {}


async def _db_save(
    session_id: str,
    user_id: str | None,
    message: str,
    reply: str,
    intent: str,
) -> None:
    try:
        from app.db import chat_history_repository
        await chat_history_repository.save_turn(
            session_id=session_id,
            user_id=user_id,
            user_message=message,
            assistant_reply=reply,
            intent=intent,
        )
    except Exception as exc:
        logger.warning(f"[{session_id}] save_result | db_error={exc!r}")
