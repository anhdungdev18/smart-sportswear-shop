from __future__ import annotations

import asyncio

from app.retrieval.product.parser.query_parser import parse_query
from app.retrieval.product.query_rewrite.ambiguity_detector import needs_pre_retrieval_rewrite
from app.retrieval.product.query_rewrite.llm_rewriter import _preserves_explicit_constraints


def test_occasion_query_requires_pre_retrieval_rewrite():
    query = "tìm áo đi Đà Lạt"
    assert needs_pre_retrieval_rewrite(query, parse_query(query)) is True


def test_clear_product_query_skips_pre_retrieval_rewrite():
    query = "tìm áo chạy bộ nam màu đen dưới 1 triệu"
    assert needs_pre_retrieval_rewrite(query, parse_query(query)) is False


def test_rewrite_must_preserve_explicit_constraints():
    original = "áo nam màu đen đi Đà Lạt dưới 1 triệu"
    assert _preserves_explicit_constraints(
        original,
        "áo khoác giữ ấm nam màu đen dưới 1 triệu",
    )
    assert not _preserves_explicit_constraints(original, "áo khoác giữ ấm nữ màu trắng")


def test_product_search_uses_ambiguous_rewrite_before_original(monkeypatch):
    from app.schemas.product import AppliedFilters, ProductSearchResult
    from app.services import product_search_service as service

    calls: list[str] = []

    async def fake_rewrite(query: str) -> str:
        return "áo khoác giữ ấm"

    async def fake_pipeline(query: str, limit: int, parsed=None) -> ProductSearchResult:
        calls.append(query)
        total = 1 if query == "áo khoác giữ ấm" else 0
        return ProductSearchResult(items=[], total=total, appliedFilters=AppliedFilters())

    monkeypatch.setattr(service.llm_rewriter, "rewrite_ambiguous_need", fake_rewrite)
    monkeypatch.setattr(service, "_pipeline", fake_pipeline)

    result = asyncio.run(service.search("tìm áo đi Đà Lạt"))

    assert result.total == 1
    assert calls == ["áo khoác giữ ấm"]
