from scripts.normalize_product_search_data import classify_color


def test_edition_name_does_not_create_false_color_match() -> None:
    assert classify_color("Shadow") == (None, "Shadow")


def test_color_terms_use_word_boundaries() -> None:
    assert classify_color("Đỏ (United)") == ("RED", None)
    assert classify_color("R009 All Black") == ("BLACK", None)
