"""Unit tests for memory/context_resolver.py"""
from __future__ import annotations

import pytest
from app.memory.context_resolver import resolve_product_reference, parse_variant_hints


# ── Ordinal resolution ────────────────────────────────────────────────────────

def _ctx(ids=None, summary=None, selected=None):
    return {
        "last_product_ids": ids or [],
        "last_products_summary": summary or [],
        "selected_product_id": selected,
    }


def test_ordinal_first():
    ctx = _ctx(ids=["id1", "id2", "id3"])
    assert resolve_product_reference("mẫu đầu tiên", ctx) == "id1"
    assert resolve_product_reference("mẫu thứ nhất", ctx) == "id1"


def test_ordinal_second():
    ctx = _ctx(ids=["id1", "id2", "id3"])
    assert resolve_product_reference("mẫu thứ 2", ctx) == "id2"
    assert resolve_product_reference("mẫu thứ hai", ctx) == "id2"


def test_ordinal_third():
    ctx = _ctx(ids=["id1", "id2", "id3"])
    assert resolve_product_reference("mẫu thứ 3", ctx) == "id3"


def test_ordinal_out_of_range():
    ctx = _ctx(ids=["id1"])
    result = resolve_product_reference("mẫu thứ 2", ctx)
    assert result is None  # index 1 out of range, should not fall through


# ── Proximal resolution ───────────────────────────────────────────────────────

def test_proximal_this():
    ctx = _ctx(ids=["id1"], selected="selected_id")
    assert resolve_product_reference("cái này", ctx) == "selected_id"


def test_proximal_that_shoe():
    ctx = _ctx(ids=["id1"], selected="shoe_id")
    assert resolve_product_reference("giày đó có còn không", ctx) == "shoe_id"


def test_proximal_falls_back_to_first():
    ctx = _ctx(ids=["id1"], selected=None)
    assert resolve_product_reference("mẫu này", ctx) == "id1"


def test_proximal_empty_context():
    ctx = _ctx()
    assert resolve_product_reference("mẫu này", ctx) is None


# ── Name substring match ──────────────────────────────────────────────────────

def test_name_match():
    ctx = _ctx(summary=[{"id": "abc", "name": "Giày Nike Air Max"}, {"id": "xyz", "name": "Áo gym"}])
    # "giày nike air max" is 4+ chars, matches the first item
    result = resolve_product_reference("cho tôi xem giày nike air max", ctx)
    assert result == "abc"


def test_name_match_short_name_ignored():
    # Names < 4 chars should not match
    ctx = _ctx(summary=[{"id": "abc", "name": "áo"}])
    result = resolve_product_reference("áo đó", ctx)
    # "mẫu đó" triggers proximal, but "áo đó" is not in _THIS_REFS, falls to name match
    # "áo" is 2 chars < 4, so name match is skipped
    # → None (no proximal trigger, no ordinal, name too short)
    # Note: "áo đó" is in _THIS_REFS ("áo đó"), so it should go proximal path
    assert result is None  # no selected_product_id and no last_product_ids


def test_no_context_returns_none():
    ctx = _ctx()
    assert resolve_product_reference("tìm giày này", ctx) is None


# ── parse_variant_hints ───────────────────────────────────────────────────────

def test_size_uppercase():
    assert parse_variant_hints("size L") == {"size": "L"}
    assert parse_variant_hints("size xl") == {"size": "XL"}


def test_size_numeric():
    assert parse_variant_hints("size 42") == {"size": "42"}


def test_color_exact():
    assert parse_variant_hints("màu đen") == {"color": "đen"}
    assert parse_variant_hints("màu trắng") == {"color": "trắng"}


def test_color_longest_match_first():
    # "xanh lá" should win over "xanh"
    h = parse_variant_hints("màu xanh lá cây")
    assert h["color"] == "xanh lá"


def test_size_and_color():
    h = parse_variant_hints("size M màu đen")
    assert h["size"] == "M"
    assert h["color"] == "đen"


def test_no_hints():
    assert parse_variant_hints("tôi muốn xem sản phẩm") == {}
