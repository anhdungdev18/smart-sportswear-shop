from __future__ import annotations

from typing import Any
from app.tools.registry import ToolDefinition

DEFINITION = ToolDefinition(
    name="answer_knowledge",
    description="Trả lời câu hỏi về chính sách đổi trả, giao hàng, size guide, FAQ.",
    capability="knowledge_qa",
    requires_auth=False,
    requires_confirmation=False,
    input_schema={"query": "string"},
)


async def run(args: dict[str, Any]) -> dict[str, Any]:
    # TODO Phase 4: replace with KnowledgeSearchService + vector/keyword retrieval
    return {
        "answer": (
            "Chính sách đổi trả của shop: trong vòng 7 ngày kể từ ngày nhận hàng, "
            "sản phẩm còn nguyên tem nhãn, chưa qua sử dụng."
        ),
        "source": "policy_faq_mock",
        "query": args.get("query", ""),
        "_mock": True,
    }
