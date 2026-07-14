"""Unit tests for retrieval/product/parser/query_parser.py"""
from __future__ import annotations

import pytest
from app.retrieval.product.parser.query_parser import parse_query


# ── Product type detection ────────────────────────────────────────────────────

@pytest.mark.parametrize("query,expected", [
    ("giày chạy bộ",          "FOOTWEAR"),
    ("áo thể thao nam",       "APPAREL"),
    ("quần tập gym",          "APPAREL"),
    ("phụ kiện bóng đá",      "ACCESSORY"),
    ("balo thể thao",         "ACCESSORY"),
    ("dụng cụ tập gym",       "EQUIPMENT"),
    ("giày sneaker",          "FOOTWEAR"),
    ("mua sắm chung",         None),
])
def test_product_type(query, expected):
    assert parse_query(query).product_type == expected


# ── Sport type detection ──────────────────────────────────────────────────────

@pytest.mark.parametrize("query,expected", [
    ("giày bóng đá",          "bóng đá"),
    ("đồ tập chạy bộ",        "chạy bộ"),
    ("quần áo gym",           "gym"),
    ("vợt cầu lông",          "cầu lông"),
    ("áo tennis",             "tennis"),
    ("giày bóng rổ",          "bóng rổ"),
    ("đồ bơi lội",            "bơi lội"),
    ("quần yoga",             "yoga"),
    ("giày leo núi",          "leo núi"),
    ("áo đạp xe",             "đạp xe"),
    ("sản phẩm không rõ",     None),
])
def test_sport_type(query, expected):
    assert parse_query(query).sport_type_hint == expected


# ── Gender detection ──────────────────────────────────────────────────────────

@pytest.mark.parametrize("query,expected", [
    ("áo nam",                "MEN"),
    ("quần nữ",               "WOMEN"),
    ("đồ unisex",             "UNISEX"),
    ("giày cho cả hai",       "UNISEX"),
    ("giày chạy bộ",          None),
])
def test_gender(query, expected):
    assert parse_query(query).gender == expected


# ── Price parsing ─────────────────────────────────────────────────────────────

def test_price_max_under():
    p = parse_query("giày dưới 500k")
    assert p.price_max == 500_000
    assert p.price_min is None


def test_price_max_million():
    p = parse_query("áo không quá 1 triệu")
    assert p.price_max == 1_000_000


def test_price_min_above():
    p = parse_query("giày trên 2 triệu")
    assert p.price_min == 2_000_000
    assert p.price_max is None


def test_price_range():
    p = parse_query("từ 300k đến 800k")
    assert p.price_min == 300_000
    assert p.price_max == 800_000


def test_price_range_million():
    p = parse_query("từ 1 triệu đến 3 triệu")
    assert p.price_min == 1_000_000
    assert p.price_max == 3_000_000


def test_no_price():
    p = parse_query("áo thể thao")
    assert p.price_min is None
    assert p.price_max is None


# ── Feature hints ─────────────────────────────────────────────────────────────

def test_feature_hints_multiple():
    p = parse_query("áo thoáng khí nhẹ chống nước")
    assert "thoáng khí" in p.feature_hints
    assert "nhẹ" in p.feature_hints
    assert "chống nước" in p.feature_hints


def test_feature_hints_empty():
    p = parse_query("áo đá bóng")
    assert p.feature_hints == []


# ── Normalization ─────────────────────────────────────────────────────────────

def test_normalized_lowercase():
    p = parse_query("GIÀY CHẠY BỘ")
    assert p.normalized == "giày chạy bộ"


def test_normalized_collapses_spaces():
    p = parse_query("giày   chạy    bộ")
    assert p.normalized == "giày chạy bộ"


# ── Combined ──────────────────────────────────────────────────────────────────

def test_full_query():
    p = parse_query("giày chạy bộ nam nhẹ dưới 1 triệu")
    assert p.product_type == "FOOTWEAR"
    assert p.sport_type_hint == "chạy bộ"
    assert p.gender == "MEN"
    assert "nhẹ" in p.feature_hints
    assert p.price_max == 1_000_000
