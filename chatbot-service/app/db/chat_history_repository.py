"""
Persistent conversation history — reads and writes the chat_messages table.

The in-memory session store holds the hot chat_history list.
This module provides the durable backing store: load on cold start, save on every turn.

All functions are fail-safe: they return empty / None and log a warning on any DB error
so that a DB outage never interrupts the chat API response path.
"""
from __future__ import annotations

from app.db.pool import get_pool, get_write_pool
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_INSERT_SQL = """
    INSERT INTO chat_messages (session_id, user_id, role, content, intent, created_at)
    VALUES ($1, $2, $3, $4, $5, now())
"""

_SELECT_SQL = """
    SELECT role, content
    FROM chat_messages
    WHERE session_id = $1
    ORDER BY created_at DESC
    LIMIT $2
"""


async def load_last_messages(session_id: str, limit: int = 10) -> list[dict]:
    """
    Load the last `limit` messages for a session, ordered oldest-first.
    Returns [] when pool is unavailable, table missing, or any error.
    """
    pool = get_pool()
    if pool is None:
        return []
    try:
        async with pool.acquire() as conn:
            rows = await conn.fetch(_SELECT_SQL, session_id, limit)
        # rows come back newest-first; reverse to oldest-first for LLM context
        return [{"role": row["role"], "content": row["content"]} for row in reversed(rows)]
    except Exception as exc:
        logger.warning(f"chat_history_repository | load_error={exc!r} session={session_id}")
        return []


async def save_turn(
    session_id: str,
    user_id: str | None,
    user_message: str,
    assistant_reply: str,
    intent: str,
) -> None:
    """
    Persist a full turn (user message + assistant reply) to the DB.
    The caller tracks this coroutine so graceful shutdown can drain pending writes.
    """
    pool = get_write_pool()
    if pool is None:
        return
    try:
        async with pool.acquire() as conn:
            async with conn.transaction():
                await conn.execute(_INSERT_SQL, session_id, user_id, "user",      user_message,    None)
                await conn.execute(_INSERT_SQL, session_id, user_id, "assistant", assistant_reply, intent)
    except Exception as exc:
        logger.warning(f"chat_history_repository | save_error={exc!r} session={session_id}")
