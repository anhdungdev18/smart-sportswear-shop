from __future__ import annotations

from pydantic import BaseModel


class KnowledgeAnswerItem(BaseModel):
    title: str      # section heading
    content: str    # section body text
    source: str     # filename (e.g. "return-policy.md")
    section: str    # same as title, explicit for clarity


class KnowledgeSearchResult(BaseModel):
    answers: list[KnowledgeAnswerItem]
    total: int
    query: str
