from __future__ import annotations

from app.repositories.knowledge_repository import KnowledgeChunk
from app.schemas.knowledge import KnowledgeAnswerItem, KnowledgeSearchResult


def format_results(chunks: list[KnowledgeChunk], query: str) -> KnowledgeSearchResult:
    answers = [
        KnowledgeAnswerItem(
            title=chunk.section,
            content=chunk.content,
            source=chunk.source,
            section=chunk.section,
        )
        for chunk in chunks
    ]
    return KnowledgeSearchResult(
        answers=answers,
        total=len(answers),
        query=query,
    )
