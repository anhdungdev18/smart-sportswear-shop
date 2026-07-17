from __future__ import annotations

from datetime import datetime, timezone

from app.memory.store_factory import get_store


async def get_context(session_id: str) -> dict:
    """Load session context dict for the given session."""
    return await get_store().get(session_id)


async def save_context(session_id: str, ctx: dict) -> None:
    """Persist a full session context dict."""
    await get_store().save(session_id, ctx)


async def update_context(session_id: str, **kwargs) -> None:
    """Patch individual fields in the session context."""
    await get_store().update(session_id, **kwargs)


async def clear_pending(session_id: str) -> None:
    """Clear pending_action / payload / created_at from the session."""
    await get_store().clear_pending(session_id)


def now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()
