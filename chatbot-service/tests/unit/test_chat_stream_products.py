from app.api.chat_stream import _extract_products


def _item(name: str, slug: str, price: int) -> dict:
    return {
        "name": name,
        "slug": slug,
        "priceMin": price,
        "primaryImage": f"/{slug}.jpg",
    }


def test_extracts_products_from_secondary_search_on_first_compound_turn():
    state = {
        "reply": "Bạn có thể tham khảo Giày Nike A và Giày Nike B.",
        "tool_result": {"source": "rule_based", "suggestedSize": "42"},
        "secondary_results": {
            "search_products": {
                "items": [
                    _item("Giày Nike A", "nike-a", 2_000_000),
                    _item("Giày Nike B", "nike-b", 2_500_000),
                ]
            }
        },
    }

    assert [product["slug"] for product in _extract_products(state)] == ["nike-a", "nike-b"]


def test_product_cards_follow_the_order_used_in_the_bot_reply():
    state = {
        "reply": "1. Giày Nike B\n2. Giày Nike A",
        "tool_result": {
            "items": [
                _item("Giày Nike A", "nike-a", 2_000_000),
                _item("Giày Nike B", "nike-b", 2_500_000),
            ]
        },
    }

    assert [product["slug"] for product in _extract_products(state)] == ["nike-b", "nike-a"]


def test_duplicate_products_from_primary_and_secondary_results_are_removed():
    shared = _item("Giày Nike A", "nike-a", 2_000_000)
    state = {
        "reply": "Giày Nike A",
        "tool_result": {"items": [shared]},
        "secondary_results": {"search_products": {"items": [shared]}},
    }

    assert [product["slug"] for product in _extract_products(state)] == ["nike-a"]


def test_only_products_named_in_the_bot_reply_are_rendered():
    state = {
        "reply": "1. Giày Nike B\n2. Giày Nike C\n3. Giày Nike A",
        "tool_result": {
            "items": [
                _item("Giày Nike A", "nike-a", 2_000_000),
                _item("Giày Nike B", "nike-b", 2_100_000),
                _item("Giày Nike C", "nike-c", 2_200_000),
                _item("Giày Nike D", "nike-d", 2_300_000),
                _item("Giày Nike E", "nike-e", 2_400_000),
            ]
        },
    }

    assert [product["slug"] for product in _extract_products(state)] == [
        "nike-b",
        "nike-c",
        "nike-a",
    ]
