"""
load_context node — tải chat_history từ DB nếu session chưa có (cold-start).

Chạy trong graph sau input_guard, trước normalize.
Tách khỏi chat.py để graph tự quản lý context, giống DigiAI load_context_node.
"""
from __future__ import annotations

from app.graph.state import AgentState
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_MAX_HISTORY = 10


async def load_context_node(state: AgentState) -> dict:
    # Nếu session đã có history (từ in-memory store) → skip
    if state.get("chat_history"):
        return {}

    # Brand-new client session (first message after minting the id) → the DB has
    # nothing to restore, so skip the ~2s remote round-trip entirely. Safe because
    # a just-created session id cannot have prior turns saved anywhere.
    if state.get("is_new_session"):
        return {}

    session_id = state.get("session_id")
    if not session_id:
        return {}

    try:
        from app.db import chat_history_repository
        db_history = await chat_history_repository.load_last_messages(
            session_id, state.get("user_id"), limit=_MAX_HISTORY
        )
        if db_history:
            logger.info(
                f"[{session_id}] load_context | restored from DB turns={len(db_history) // 2}"
            )
            return {"chat_history": db_history}
    except Exception as exc:
        logger.warning(f"[{session_id}] load_context | DB fallback failed: {exc!r}")

    return {}
