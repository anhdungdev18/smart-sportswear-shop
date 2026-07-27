from __future__ import annotations

from app.tools.registry import ToolDefinition
from app.services.recommendation_service import recommend_related, recommend_by_need

DEFINITION = ToolDefinition(
    name="recommend_products",
    description="Gợi ý sản phẩm liên quan đến sản phẩm đang xem, hoặc gợi ý theo nhu cầu mô tả",
    capability="product_search",
    requires_auth=False,
    requires_confirmation=False,
    input_schema={
        "mode": {"type": "string", "enum": ["related", "need_based"]},
        "product_id": {"type": "string", "description": "UUID sản phẩm nguồn (cho mode related)"},
        "query": {"type": "string", "description": "Mô tả nhu cầu (cho mode need_based)"},
    },
)


async def run(args: dict) -> dict:
    mode = args.get("mode") or "need_based"
    if mode == "related":
        result = await recommend_related(args.get("product_id") or "")
    else:
        result = await recommend_by_need(args.get("query") or "")
    return result.model_dump()
