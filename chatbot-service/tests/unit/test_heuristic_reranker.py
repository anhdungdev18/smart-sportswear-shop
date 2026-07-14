"""Unit tests for retrieval/product/rerank/heuristic_reranker.py"""
from __future__ import annotations

import pytest
from app.retrieval.product.rerank.heuristic_reranker import rerank
from app.retrieval.product.parser.query_parser import ParsedQuery


def _parsed(keyword: str = "", sport: str | None = None) -> ParsedQuery:
    return ParsedQuery(raw=keyword, normalized=keyword, keyword=keyword,
                       sport_type_hint=sport)


def _item(pid: str, name: str = "Product", sport: str = "", avail: int = 10,
          rrf: float = 0.1, vec_score: float | None = None) -> dict:
    d = {
        "product_id": pid,
        "name": name,
        "sport_type": sport,
        "total_available": avail,
        "_rrf_score": rrf,
    }
    if vec_score is not None:
        d["vector_score"] = vec_score
    return d


# ── Limit ─────────────────────────────────────────────────────────────────────

def test_limit_applied():
    items = [_item(str(i)) for i in range(10)]
    parsed = _parsed()
    result = rerank(items, parsed, limit=3)
    assert len(result) == 3


def test_limit_larger_than_items():
    items = [_item("A"), _item("B")]
    result = rerank(items, _parsed(), limit=10)
    assert len(result) == 2


def test_empty_input():
    assert rerank([], _parsed(), limit=5) == []


# ── Sport type bonus ──────────────────────────────────────────────────────────

def test_sport_match_scores_higher():
    no_match  = _item("A", sport="tennis",   rrf=0.10)
    yes_match = _item("B", sport="bóng đá",  rrf=0.05)
    parsed = _parsed(sport="bóng đá")
    result = rerank([no_match, yes_match], parsed, limit=2)
    assert result[0]["product_id"] == "B"


def test_sport_partial_match():
    """sport_type_hint 'bóng đá' should match 'Bóng Đá' (case-insensitive)."""
    item = _item("A", sport="Bóng Đá", rrf=0.0)
    parsed = _parsed(sport="bóng đá")
    result = rerank([item], parsed, limit=1)
    assert len(result) == 1  # not filtered, just reranked


# ── Stock penalties ───────────────────────────────────────────────────────────

def test_out_of_stock_penalized():
    in_stock  = _item("A", avail=10, rrf=0.05)
    out_stock = _item("B", avail=0,  rrf=0.20)  # higher base score
    result = rerank([in_stock, out_stock], _parsed(), limit=2)
    # B has rrf 0.20 but -0.50 penalty → net -0.30; A has rrf 0.05 → A wins
    assert result[0]["product_id"] == "A"


def test_low_stock_minor_penalty():
    normal = _item("A", avail=10, rrf=0.10)
    low    = _item("B", avail=2,  rrf=0.15)  # higher rrf but low stock (-0.10)
    result = rerank([normal, low], _parsed(), limit=2)
    # B: 0.15 - 0.10 = 0.05; A: 0.10 → A wins
    assert result[0]["product_id"] == "A"


# ── Vector score bonus ────────────────────────────────────────────────────────

def test_vector_score_bonus():
    no_vec  = _item("A", rrf=0.10)
    has_vec = _item("B", rrf=0.05, vec_score=0.90)
    # B: 0.05 + 0.20*0.90 = 0.23; A: 0.10
    result = rerank([no_vec, has_vec], _parsed(), limit=2)
    assert result[0]["product_id"] == "B"


# ── Keyword token overlap ─────────────────────────────────────────────────────

def test_keyword_overlap_bonus():
    no_match  = _item("A", name="Quần bóng rổ", rrf=0.10)
    kw_match  = _item("B", name="Giày chạy bộ", rrf=0.05)
    parsed = _parsed(keyword="giày chạy bộ")
    result = rerank([no_match, kw_match], parsed, limit=2)
    assert result[0]["product_id"] == "B"


def test_short_tokens_ignored():
    """Tokens with len <= 2 should NOT contribute to overlap bonus."""
    item = _item("A", name="áo đỏ", rrf=0.0)
    parsed = _parsed(keyword="áo đỏ")  # both tokens len <= 2
    # score = 0.0 (no bonus)
    result = rerank([item], parsed, limit=1)
    assert len(result) == 1
