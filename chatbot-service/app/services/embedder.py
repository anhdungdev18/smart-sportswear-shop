"""
Embedding service — OpenAI text-embedding-3-small (1536-dim dense vectors).

Uses the OpenAI embeddings API. Returns None if no API key is configured or the
API call fails; callers must treat None as "vector search unavailable" and fall
back to keyword-only search.

Public async API is unchanged (embed / embed_batch) so callers need no edits.
"""
from __future__ import annotations

import hashlib
import json
from collections import OrderedDict

from app.config.settings import settings
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

MODEL_NAME     = settings.EMBEDDING_MODEL      # "text-embedding-3-small"
EMBEDDING_DIMS = settings.EMBEDDING_DIMS       # 1536
_MEMORY_CACHE_MAX = 256
_memory_cache: OrderedDict[str, list[float]] = OrderedDict()


def _is_available() -> bool:
    key = settings.OPENAI_API_KEY
    # Reject empty and the placeholder value shipped in .env.example
    return bool(key) and not key.startswith("sk-...")


async def embed(text: str) -> list[float] | None:
    """Embed a single text. Returns None if API key missing or call fails."""
    if not _is_available():
        logger.info("embedder | skip reason=no_openai_api_key")
        return None
    cache = None
    cache_key = (
        f"search:embedding:{MODEL_NAME}:"
        f"{hashlib.sha256(' '.join(text.casefold().split()).encode('utf-8')).hexdigest()}"
    )
    memory_value = _memory_cache.get(cache_key)
    if memory_value is not None:
        _memory_cache.move_to_end(cache_key)
        return memory_value
    if settings.REDIS_URL:
        try:
            from redis.asyncio import Redis

            cache = Redis.from_url(
                settings.REDIS_URL,
                socket_connect_timeout=0.15,
                socket_timeout=0.15,
                decode_responses=True,
            )
            cached = await cache.get(cache_key)
            if cached:
                vector = json.loads(cached)
                if len(vector) == EMBEDDING_DIMS:
                    _remember(cache_key, vector)
                    await cache.aclose()
                    return vector
        except Exception:
            cache = None
    try:
        from openai import AsyncOpenAI

        client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY)
        resp = await client.embeddings.create(
            model=MODEL_NAME,
            input=[text],
            dimensions=EMBEDDING_DIMS,
        )
        vector = resp.data[0].embedding
        if len(vector) != EMBEDDING_DIMS:
            raise ValueError(f"Embedding dimension mismatch: expected {EMBEDDING_DIMS}, got {len(vector)}")
        _remember(cache_key, vector)
        if cache is not None:
            try:
                await cache.setex(
                    cache_key,
                    settings.PRODUCT_SEARCH_QUERY_CACHE_TTL_SECONDS,
                    json.dumps(vector, separators=(",", ":")),
                )
            except Exception:
                pass
            finally:
                await cache.aclose()
        return vector
    except Exception as exc:
        if cache is not None:
            try:
                await cache.aclose()
            except Exception:
                pass
        logger.warning(f"embedder | embed_error={exc!r}")
        return None


def _remember(key: str, vector: list[float]) -> None:
    _memory_cache[key] = vector
    _memory_cache.move_to_end(key)
    while len(_memory_cache) > _MEMORY_CACHE_MAX:
        _memory_cache.popitem(last=False)


async def embed_batch(texts: list[str]) -> list[list[float]] | None:
    """Embed a list of texts in one API call. Returns None on failure, [] for empty input."""
    if not texts:
        return []
    if not _is_available():
        logger.info("embedder | skip reason=no_openai_api_key")
        return None
    try:
        from openai import AsyncOpenAI

        client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY)
        resp = await client.embeddings.create(
            model=MODEL_NAME,
            input=texts,
            dimensions=EMBEDDING_DIMS,
        )
        # API preserves input order in resp.data
        vectors = [item.embedding for item in resp.data]
        if any(len(vector) != EMBEDDING_DIMS for vector in vectors):
            raise ValueError(f"Embedding dimension mismatch: expected {EMBEDDING_DIMS}")
        return vectors
    except Exception as exc:
        logger.warning(f"embedder | embed_batch_error={exc!r}")
        return None
