from __future__ import annotations

from app.retrieval.knowledge.retrieve.knowledge_retriever import retrieve
from app.retrieval.knowledge.formatter.knowledge_formatter import format_results
from app.schemas.knowledge import KnowledgeSearchResult
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_LIMIT = 3


def search(query: str, limit: int = _LIMIT) -> KnowledgeSearchResult:
    """
    Knowledge retrieval pipeline (Phase 4):
      retrieve (keyword match) → format
    Synchronous — no DB, file-based corpus loaded in memory.
    """
    logger.info(f"knowledge_search | query={query!r}")

    chunks = retrieve(query, limit=limit)

    if not chunks:
        logger.info(f"knowledge_search | no_results query={query!r}")

    result = format_results(chunks, query)
    logger.info(f"knowledge_search | total={result.total}")
    return result
