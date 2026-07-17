"""
Embedding service — OpenAI text-embedding-3-small (1536-dim dense vectors).

Uses the OpenAI embeddings API. Returns None if no API key is configured or the
API call fails; callers must treat None as "vector search unavailable" and fall
back to keyword-only search.

Public async API is unchanged (embed / embed_batch) so callers need no edits.
"""
from __future__ import annotations

from app.config.settings import settings
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

MODEL_NAME     = settings.EMBEDDING_MODEL      # "text-embedding-3-small"
EMBEDDING_DIMS = settings.EMBEDDING_DIMS       # 1536


def _is_available() -> bool:
    key = settings.OPENAI_API_KEY
    # Reject empty and the placeholder value shipped in .env.example
    return bool(key) and not key.startswith("sk-...")


async def embed(text: str) -> list[float] | None:
    """Embed a single text. Returns None if API key missing or call fails."""
    if not _is_available():
        logger.info("embedder | skip reason=no_openai_api_key")
        return None
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
        return vector
    except Exception as exc:
        logger.warning(f"embedder | embed_error={exc!r}")
        return None


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
