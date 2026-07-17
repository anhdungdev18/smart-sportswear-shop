"""Unit tests for retrieval/product/fusion/rrf_fusion.py"""
from __future__ import annotations

import pytest
from app.retrieval.product.fusion.rrf_fusion import fuse


def _row(pid: str, **kw) -> dict:
    return {"product_id": pid, "name": f"Product {pid}", "total_available": 10, **kw}


# ── Basic behavior ────────────────────────────────────────────────────────────

def test_keyword_only_preserved():
    kw = [_row("A"), _row("B"), _row("C")]
    result = fuse(kw, [])
    ids = [r["product_id"] for r in result]
    assert ids == ["A", "B", "C"]


def test_empty_both_returns_empty():
    assert fuse([], []) == []


def test_dedup_same_product_in_both():
    kw = [_row("X")]
    vec = [_row("X", vector_score=0.9)]
    result = fuse(kw, vec)
    assert len(result) == 1
    assert result[0]["product_id"] == "X"


def test_rrf_score_attached():
    kw = [_row("A"), _row("B")]
    result = fuse(kw, [])
    assert all("_rrf_score" in r for r in result)
    # A (rank 1) should have higher score than B (rank 2)
    scores = {r["product_id"]: r["_rrf_score"] for r in result}
    assert scores["A"] > scores["B"]


def test_source_marked_keyword():
    kw = [_row("A")]
    result = fuse(kw, [])
    assert result[0]["_source"] == "keyword"


def test_source_marked_vector():
    vec = [_row("Z", vector_score=0.8)]
    result = fuse([], vec)
    assert result[0]["_source"] == "vector"


# ── Scoring correctness ───────────────────────────────────────────────────────

def test_both_sources_higher_score_than_single():
    """An item in both lists should outscore items in only one list (same rank)."""
    kw  = [_row("BOTH"), _row("KW_ONLY")]
    vec = [_row("BOTH"), _row("VEC_ONLY")]
    result = fuse(kw, vec)
    scores = {r["product_id"]: r["_rrf_score"] for r in result}
    assert scores["BOTH"] > scores["KW_ONLY"]
    assert scores["BOTH"] > scores["VEC_ONLY"]


def test_ordering_descending():
    kw  = [_row("A"), _row("B"), _row("C")]
    vec = [_row("C"), _row("B"), _row("A")]
    result = fuse(kw, vec)
    scores = [r["_rrf_score"] for r in result]
    assert scores == sorted(scores, reverse=True)


def test_custom_k():
    """With k=0, rank-1 items get score=1.0, rank-2 get 0.5."""
    kw = [_row("A"), _row("B")]
    result = fuse(kw, [], k=0)
    scores = {r["product_id"]: r["_rrf_score"] for r in result}
    assert abs(scores["A"] - 1.0) < 1e-9
    assert abs(scores["B"] - 0.5) < 1e-9


def test_row_data_from_keyword_source_preferred():
    """When same product in both, keyword row data is used (keyword processed first)."""
    kw  = [{"product_id": "A", "name": "From KW",  "total_available": 5}]
    vec = [{"product_id": "A", "name": "From VEC", "total_available": 3, "vector_score": 0.9}]
    result = fuse(kw, vec)
    assert result[0]["name"] == "From KW"


# ── Weight parameters ─────────────────────────────────────────────────────────

def test_vector_weight_zero_ignores_vector():
    kw  = [_row("A"), _row("B")]
    vec = [_row("C")]
    result = fuse(kw, vec, vector_weight=0.0)
    # C should still appear but with 0 contribution from vector
    ids = [r["product_id"] for r in result]
    assert "C" in ids
    c_score = next(r["_rrf_score"] for r in result if r["product_id"] == "C")
    assert c_score == 0.0
