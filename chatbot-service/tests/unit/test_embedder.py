from __future__ import annotations

from types import SimpleNamespace

import pytest

from app.services import embedder


class _FakeEmbeddings:
    def __init__(self):
        self.calls = []

    async def create(self, **kwargs):
        self.calls.append(kwargs)
        count = len(kwargs["input"])
        dims = kwargs["dimensions"]
        return SimpleNamespace(
            data=[SimpleNamespace(embedding=[0.0] * dims) for _ in range(count)]
        )


@pytest.mark.asyncio
async def test_single_embedding_requests_configured_1536_dimensions(monkeypatch):
    fake = _FakeEmbeddings()
    monkeypatch.setattr(embedder, "_is_available", lambda: True)
    monkeypatch.setattr("openai.AsyncOpenAI", lambda **_: SimpleNamespace(embeddings=fake))

    vector = await embedder.embed("giày chạy bộ")

    assert len(vector) == 1536
    assert fake.calls[0]["model"] == "text-embedding-3-small"
    assert fake.calls[0]["dimensions"] == 1536


@pytest.mark.asyncio
async def test_batch_embedding_preserves_input_count(monkeypatch):
    fake = _FakeEmbeddings()
    monkeypatch.setattr(embedder, "_is_available", lambda: True)
    monkeypatch.setattr("openai.AsyncOpenAI", lambda **_: SimpleNamespace(embeddings=fake))

    vectors = await embedder.embed_batch(["áo", "quần", "giày"])

    assert len(vectors) == 3
    assert all(len(vector) == 1536 for vector in vectors)


@pytest.mark.asyncio
async def test_embedding_without_key_falls_back_cleanly(monkeypatch):
    monkeypatch.setattr(embedder, "_is_available", lambda: False)
    assert await embedder.embed("áo thể thao") is None
