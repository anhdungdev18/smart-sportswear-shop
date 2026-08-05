from app.graph.nodes.validate_answer import validate_answer_node


def _state(reply: str, items: list[dict]) -> dict:
    return {
        "session_id": "s1",
        "intent": "PRODUCT_SEARCH",
        "reply": reply,
        "execution_blocked": False,
        "tool_result": {
            "items": items,
            "total": len(items),
            "appliedFilters": {"keyword": "vapor 16"},
        },
    }


async def test_non_empty_results_replace_false_not_found_reply():
    result = await validate_answer_node(
        _state(
            "Xin lỗi, hiện tại tôi chưa tìm được thông tin về giày Vapor 16.",
            [{"name": "Nike Mercurial Vapor 16", "priceMin": 2490000, "totalAvailable": 8}],
        )
    )

    assert "reply" in result
    assert "Nike Mercurial Vapor 16" in result["reply"]


async def test_empty_results_keep_not_found_reply():
    result = await validate_answer_node(_state("Tôi chưa tìm thấy sản phẩm phù hợp.", []))
    assert result == {}
