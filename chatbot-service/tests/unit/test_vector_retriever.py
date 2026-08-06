from __future__ import annotations

import asyncio

from app.config.settings import settings
from app.retrieval.product.filters.product_filter import ProductFilter
from app.retrieval.product.vector import vector_retriever


def test_vector_retriever_drops_results_below_configured_similarity(monkeypatch):
    rows = [
        {"product_id": "low", "vector_score": 0.29},
        {"product_id": "high", "vector_score": 0.42},
    ]

    async def fake_vector_search(**_kwargs):
        return rows

    monkeypatch.setattr(
        "app.repositories.vector_repository.vector_search",
        fake_vector_search,
    )
    monkeypatch.setattr(settings, "PRODUCT_SEARCH_MIN_SIMILARITY", 0.40)

    result = asyncio.run(vector_retriever.retrieve("áo khoác giữ ấm", ProductFilter()))

    assert [row["product_id"] for row in result] == ["high"]
