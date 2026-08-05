from app.retrieval.product.parser.query_parser import parse_query


def test_structured_parser_examples():
    parsed = parse_query("giày Nike nam dưới 2 triệu size 42", brands=["Nike"])
    assert parsed.brand == "Nike"
    assert parsed.gender == "MEN"
    assert parsed.product_type == "FOOTWEAR"
    assert parsed.price_max == 2_000_000
    assert parsed.size == "42"

    assert parse_query("giay nike nam duoi 2tr size 42").price_max == 2_000_000
    assert parse_query("áo chạy bộ nữ màu hồng").gender == "WOMEN"
    assert parse_query("áo chạy bộ nữ màu hồng").color_family == "PINK"
    assert parse_query("giày cỏ thật").surface == "FG"
    assert parse_query("giày cỏ nhân tạo").surface == "TF"
    assert parse_query("giày futsal").surface == "IC"
    assert parse_query("từ 1 triệu đến 2 triệu").price_min == 1_000_000
    assert parse_query("từ 1 triệu đến 2 triệu").price_max == 2_000_000
    assert parse_query("trên 500k").price_min == 500_000
    assert parse_query("không quá 1tr5").price_max == 1_500_000


def test_explicit_filters_override_parser():
    parsed = parse_query(
        "giày nam màu đen dưới 2 triệu",
        explicit_filters={"gender": "WOMEN", "color": "WHITE", "maxPrice": 3_000_000},
    )
    assert parsed.gender == "WOMEN"
    assert parsed.color_family == "WHITE"
    assert parsed.price_max == 3_000_000
