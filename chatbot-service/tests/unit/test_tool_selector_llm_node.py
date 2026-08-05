from unittest.mock import AsyncMock, patch

from app.graph.nodes.tool_selector_llm_node import tool_selector_llm_node


async def test_search_tool_promotes_unknown_to_product_search_intent():
    state = {
        "session_id": "s1",
        "message": "có vapor 16 không",
        "session_context": {},
        "access_token": None,
    }
    with patch(
        "app.services.tool_selector_llm.select_tool_for_unknown",
        new=AsyncMock(return_value=("search_products", {"query": "vapor 16"})),
    ):
        result = await tool_selector_llm_node(state)

    assert result["selected_tool"] == "search_products"
    assert result["intent"] == "PRODUCT_SEARCH"
    assert result["intent_confidence"] == 0.90


async def test_none_tool_keeps_original_intent_untouched():
    state = {
        "session_id": "s2",
        "message": "xin chào",
        "session_context": {},
        "access_token": None,
    }
    with patch(
        "app.services.tool_selector_llm.select_tool_for_unknown",
        new=AsyncMock(return_value=("none", {})),
    ):
        result = await tool_selector_llm_node(state)

    assert result == {"selected_tool": "none", "tool_args": {}}
