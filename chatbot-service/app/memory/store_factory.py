from __future__ import annotations

from app.memory.base_store import BaseSessionStore
from app.memory.in_memory_store import InMemorySessionStore
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_active_store: BaseSessionStore | None = None


async def init_store(redis_url: str = "", ttl: int = 3600) -> None:
    """
    Called once at app startup.
    Tries Redis first; falls back to in-memory if Redis is unavailable or not configured.
    Service never fails hard due to missing Redis.
    """
    global _active_store
    if redis_url:
        try:
            from redis.asyncio import from_url as redis_from_url
            from app.memory.redis_store import RedisSessionStore

            client = redis_from_url(redis_url, decode_responses=True)
            await client.ping()
            _active_store = RedisSessionStore(client, ttl=ttl)
            logger.info(f"session_store | backend=redis ttl={ttl}s")
            return
        except Exception as exc:
            logger.warning(f"session_store | redis_unavailable={exc!r} → fallback=in_memory")

    _active_store = InMemorySessionStore()
    logger.info("session_store | backend=in_memory")


def get_store() -> BaseSessionStore:
    global _active_store
    if _active_store is None:
        # Lazy init if init_store() was never called (e.g. tests)
        _active_store = InMemorySessionStore()
    return _active_store
