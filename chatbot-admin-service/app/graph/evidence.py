from __future__ import annotations

from dataclasses import dataclass
from typing import Any

from app.knowledge.business_rules import business_rules_for
from app.schemas.chat import Intent, QuestionType


@dataclass(frozen=True)
class EvidencePack:
    message: str
    intent: Intent
    question_type: QuestionType
    tool_calls: list[dict[str, Any]]
    business_rules: list[str]
    memory: dict[str, Any]


def build_evidence_pack(
    *,
    message: str,
    intent: Intent,
    question_type: QuestionType,
    tool_calls: list[dict[str, Any]],
    memory: dict[str, Any] | None = None,
) -> EvidencePack:
    return EvidencePack(
        message=message,
        intent=intent,
        question_type=question_type,
        tool_calls=tool_calls,
        business_rules=business_rules_for(intent),
        memory=memory or {},
    )
