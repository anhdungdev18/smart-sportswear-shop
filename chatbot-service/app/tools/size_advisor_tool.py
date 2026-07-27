from __future__ import annotations

from app.tools.registry import ToolDefinition
from app.services.size_advisor_service import advise

DEFINITION = ToolDefinition(
    name="size_advisor",
    description="Tư vấn size từ variant thật của sản phẩm, bảng size kiến thức, hoặc ước lượng theo số đo cơ thể",
    capability="product_search",
    requires_auth=False,
    requires_confirmation=False,
    input_schema={
        "message": {"type": "string", "description": "Câu hỏi của user (để parse số đo/size)"},
        "product_id": {"type": "string", "description": "UUID sản phẩm nếu hỏi về sản phẩm cụ thể"},
    },
)


async def run(args: dict) -> dict:
    result = await advise(args.get("message") or "", args.get("product_id") or None)
    return result.model_dump()
