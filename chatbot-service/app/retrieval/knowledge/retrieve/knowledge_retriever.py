from __future__ import annotations

from app.repositories.knowledge_repository import KnowledgeChunk, search
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)


def retrieve(query: str, limit: int = 3) -> list[KnowledgeChunk]:
    """
    Keyword retrieval over internal knowledge corpus.
    Returns top matching chunks with source attribution.
    """
    chunks = search(query, limit=limit)
    sources = [f"{c.source}§{c.section}" for c in chunks]
    logger.info(f"knowledge_retriever | query={query!r} matched={sources}")
    return chunks
