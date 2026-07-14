from __future__ import annotations

import json

from app.memory.base_store import BaseSessionStore, default_context
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)


class RedisSessionStore(BaseSessionStore):
    def __init__(self, redis_client, ttl: int = 3600) -> None:
        self._redis = redis_client
        self._ttl = ttl
        self._prefix = "chatbot:session:"

    def _key(self, session_id: str) -> str:
        return f"{self._prefix}{session_id}"

    async def get(self, session_id: str) -> dict:
        try:
            raw = await self._redis.get(self._key(session_id))
            if raw:
                stored = json.loads(raw)
                base = default_context()
                base.update(stored)   # forward-compat: new keys get their defaults
                return base
        except Exception as exc:
            logger.warning(f"redis_store | get_error={exc!r} session_id={session_id}")
        return default_context()

    async def save(self, session_id: str, ctx: dict) -> None:
        try:
            await self._redis.setex(
                self._key(session_id),
                self._ttl,
                json.dumps(ctx, ensure_ascii=False, default=str),
            )
        except Exception as exc:
            logger.warning(f"redis_store | save_error={exc!r} session_id={session_id}")
