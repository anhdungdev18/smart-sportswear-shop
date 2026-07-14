"""Unit tests for retrieval/product/query_rewrite/synonym_rewriter.py"""
from __future__ import annotations

import pytest
from app.retrieval.product.query_rewrite.synonym_rewriter import rewrite


# ── Rules that should match ───────────────────────────────────────────────────

@pytest.mark.parametrize("query,expected_contains", [
    ("áo đi đà lạt",              "giữ ấm"),
    ("giày đá banh sân cỏ nhân tạo", "turf"),
    ("đồ tập nữ",                 "quần áo tập gym nữ"),
    ("đồ tập gym",                "quần áo tập gym"),
    ("đồ chạy bộ",                "quần áo chạy bộ"),
    ("đồ chạy",                   "quần áo chạy bộ"),
    ("outfit tập",                "bộ quần áo tập"),
    ("set đồ tập",                "quần áo tập"),  # "đồ tập" rule fires first → "set quần áo tập"
    ("giày chạy",                 "giày chạy bộ"),
])
def test_rewrite_matches(query, expected_contains):
    result = rewrite(query)
    assert expected_contains in result, f"rewrite({query!r}) = {result!r}"


# ── Queries that must NOT be rewritten ───────────────────────────────────────

@pytest.mark.parametrize("query", [
    "giày thể thao nam",       # no matching rule
    "mua áo thể thao",
    "hỏi về chính sách đổi trả",
])
def test_no_rewrite_when_no_rule(query):
    result = rewrite(query)
    # Either returns original or a non-harmful rewrite; must not add brand/gender/price
    assert "Nike" not in result
    assert "Adidas" not in result
    assert "triệu" not in result


# ── Safety: no false additions ────────────────────────────────────────────────

def test_rewrite_does_not_add_brand():
    result = rewrite("giày chạy")
    assert "nike" not in result.lower()
    assert "adidas" not in result.lower()


def test_rewrite_does_not_add_price():
    result = rewrite("đồ tập nữ")
    assert "triệu" not in result
    assert "đồng" not in result


def test_rewrite_does_not_add_gender_when_absent():
    # "đồ tập gym" should not add "nam" or "nữ" on its own
    result = rewrite("đồ tập gym")
    # The rule maps "đồ tập gym" → "quần áo tập gym" (no gender added)
    assert result == "quần áo tập gym"


# ── Identity cases ────────────────────────────────────────────────────────────

def test_returns_original_when_no_match():
    q = "bóng bàn chuyên nghiệp"
    assert rewrite(q) == q


def test_case_insensitive():
    # Input lowercased by rewrite()
    result = rewrite("ĐỒ TẬP NỮ")
    assert "quần áo tập gym nữ" in result
