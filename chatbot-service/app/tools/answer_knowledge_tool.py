from __future__ import annotations

from typing import Any
from app.tools.registry import ToolDefinition
from app.services import knowledge_search_service

DEFINITION = ToolDefinition(
    name="answer_knowledge",
    description="Trả lời câu hỏi về chính sách đổi trả, giao hàng, size guide, bảo quản, FAQ.",
    capability="knowledge_qa",
    requires_auth=False,
    requires_confirmation=False,
    input_schema={"query": "string"},
)


async def run(args: dict[str, Any]) -> dict[str, Any]:
    query = args.get("query", "")
    result = knowledge_search_service.search(query)
    return result.model_dump()
