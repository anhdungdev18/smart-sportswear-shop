from __future__ import annotations

from abc import ABC, abstractmethod
from datetime import datetime, timezone

PENDING_ACTION_TTL_SECONDS = 300  # 5 minutes


def default_context() -> dict:
    return {
        "last_intent": "",
        "last_product_ids": [],
        "last_products_summary": [],
        "selected_product_id": None,
        "selected_product_name": None,
        "selected_variant_hints": {},
        # Phase 9: pending confirmation state
        "pending_action": None,
        "pending_action_payload": {},
        "pending_action_created_at": None,
        # Phase 10: conversation history for LLM context (last 10 messages)
        "chat_history": [],
    }


def is_pending_expired(created_at_str: str | None) -> bool:
    if not created_at_str:
        return True
    try:
        created = datetime.fromisoformat(created_at_str)
        if created.tzinfo is None:
            created = created.replace(tzinfo=timezone.utc)
        age = (datetime.now(timezone.utc) - created).total_seconds()
        return age > PENDING_ACTION_TTL_SECONDS
    except Exception:
        return True


class BaseSessionStore(ABC):
    @abstractmethod
    async def get(self, session_id: str) -> dict:
        """Return session context dict; defaults filled if session is new."""

    @abstractmethod
    async def save(self, session_id: str, ctx: dict) -> None:
        """Persist session context dict."""

    async def update(self, session_id: str, **kwargs) -> None:
        ctx = await self.get(session_id)
        ctx.update(kwargs)
        await self.save(session_id, ctx)

    async def clear_pending(self, session_id: str) -> None:
        await self.update(
            session_id,
            pending_action=None,
            pending_action_payload={},
            pending_action_created_at=None,
        )
