"""Unit tests for retrieval/product/guards/result_guard.py"""
from __future__ import annotations

import pytest
from app.retrieval.product.guards.result_guard import apply_guards
from app.retrieval.product.filters.product_filter import ProductFilter


def _filter(**kw) -> ProductFilter:
    return ProductFilter(**kw)


def _item(pid: str, avail: int = 5, ptype: str = "APPAREL",
          gender: str | None = None) -> dict:
    return {
        "product_id": pid,
        "total_available": avail,
        "product_type": ptype,
        "gender": gender,
    }


# ── Stock guard ───────────────────────────────────────────────────────────────

def test_removes_out_of_stock():
    items = [_item("A", avail=5), _item("B", avail=0), _item("C", avail=1)]
    result = apply_guards(items, _filter())
    ids = [r["product_id"] for r in result]
    assert "B" not in ids
    assert "A" in ids
    assert "C" in ids


def test_keeps_in_stock():
    items = [_item("A", avail=100)]
    result = apply_guards(items, _filter())
    assert len(result) == 1


# ── Product type guard ────────────────────────────────────────────────────────

def test_type_filter_removes_wrong_type():
    items = [_item("A", ptype="APPAREL"), _item("B", ptype="FOOTWEAR")]
    result = apply_guards(items, _filter(product_type="FOOTWEAR"))
    ids = [r["product_id"] for r in result]
    assert "B" in ids
    assert "A" not in ids


def test_type_filter_none_keeps_all():
    items = [_item("A", ptype="APPAREL"), _item("B", ptype="FOOTWEAR")]
    result = apply_guards(items, _filter(product_type=None))
    assert len(result) == 2


# ── Gender guard ──────────────────────────────────────────────────────────────

def test_gender_men_removes_women():
    items = [
        _item("A", gender="MEN"),
        _item("B", gender="WOMEN"),
        _item("C", gender="UNISEX"),
        _item("D", gender=None),
    ]
    result = apply_guards(items, _filter(gender="MEN"))
    ids = [r["product_id"] for r in result]
    assert "B" not in ids
    assert "A" in ids
    assert "C" in ids
    assert "D" in ids  # None gender kept


def test_gender_women_removes_men():
    items = [
        _item("A", gender="MEN"),
        _item("B", gender="WOMEN"),
    ]
    result = apply_guards(items, _filter(gender="WOMEN"))
    ids = [r["product_id"] for r in result]
    assert "A" not in ids
    assert "B" in ids


def test_gender_none_keeps_all():
    items = [_item("A", gender="MEN"), _item("B", gender="WOMEN")]
    result = apply_guards(items, _filter(gender=None))
    assert len(result) == 2


def test_gender_unisex_filter_keeps_all_non_opposite():
    items = [
        _item("A", gender="MEN"),
        _item("B", gender="WOMEN"),
        _item("C", gender="UNISEX"),
    ]
    result = apply_guards(items, _filter(gender="UNISEX"))
    # UNISEX is not MEN or WOMEN, so no gender guard fires
    assert len(result) == 3


# ── Ordering: stock guard fires first ────────────────────────────────────────

def test_guards_stacked():
    items = [
        _item("A", avail=0,  ptype="FOOTWEAR", gender="MEN"),  # out-of-stock
        _item("B", avail=5,  ptype="APPAREL",  gender="MEN"),  # wrong type
        _item("C", avail=5,  ptype="FOOTWEAR", gender="WOMEN"),# wrong gender
        _item("D", avail=5,  ptype="FOOTWEAR", gender="MEN"),  # passes all
    ]
    result = apply_guards(items, _filter(product_type="FOOTWEAR", gender="MEN"))
    ids = [r["product_id"] for r in result]
    assert ids == ["D"]
