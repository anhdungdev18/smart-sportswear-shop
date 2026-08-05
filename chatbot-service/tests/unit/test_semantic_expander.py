from app.retrieval.product.query_rewrite.semantic_expander import expand


def test_expands_catalog_term_without_removing_original_query():
    query = "áo dài tay chạy trời lạnh"
    result = expand(query)
    assert result.startswith(query)
    assert "L/S long sleeve" in result


def test_leaves_unrelated_query_unchanged():
    assert expand("điện thoại iphone") == "điện thoại iphone"
