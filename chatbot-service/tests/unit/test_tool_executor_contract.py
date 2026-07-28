from __future__ import annotations

from unittest.mock import AsyncMock, patch

from app.graph.nodes.tool_executor import _run_one


class _Result:
    def __init__(self, payload: dict):
        self.payload = payload

    def model_dump(self) -> dict:
        return self.payload


async def test_product_detail_tool_receives_argument_dict():
    service = AsyncMock(return_value=_Result({"found": True, "productId": "p1"}))
    with patch("app.tools.get_product_detail_tool.get_detail", service):
        _, result, error = await _run_one(
            "s1",
            "get_product_detail",
            {"product_id": "p1", "size_hint": "L", "color_hint": "Đen"},
        )
    service.assert_awaited_once_with("p1", "L", "Đen")
    assert error is None
    assert result == {"found": True, "productId": "p1"}


async def test_recommendation_tool_receives_argument_dict():
    service = AsyncMock(return_value=_Result({"mode": "related", "items": []}))
    with patch("app.tools.recommend_products_tool.recommend_related", service):
        _, result, error = await _run_one(
            "s1", "recommend_products", {"mode": "related", "product_id": "p1"}
        )
    service.assert_awaited_once_with("p1")
    assert error is None
    assert result == {"mode": "related", "items": []}


async def test_size_advisor_tool_receives_argument_dict():
    service = AsyncMock(return_value=_Result({"source": "rule_based", "suggestedSize": "L"}))
    with patch("app.tools.size_advisor_tool.advise", service):
        _, result, error = await _run_one(
            "s1", "size_advisor", {"message": "cao 1m70 nặng 65kg", "product_id": None}
        )
    service.assert_awaited_once_with("cao 1m70 nặng 65kg", None)
    assert error is None
    assert result == {"source": "rule_based", "suggestedSize": "L"}
