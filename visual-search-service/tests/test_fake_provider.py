import asyncio
import math

import pytest

from app.providers.base import EmbeddingResult, EmbeddingUsage
from app.providers.fake import FakeEmbeddingProvider


def test_fake_provider_is_deterministic_and_normalized() -> None:
    provider = FakeEmbeddingProvider(dimensions=16)
    first = asyncio.run(provider.embed_query(b"same-image"))
    second = asyncio.run(provider.embed_query(b"same-image"))

    assert first == second
    assert first.dimensions == 16
    assert math.sqrt(sum(value * value for value in first.vector)) == pytest.approx(1.0)


def test_document_and_query_embeddings_use_distinct_domains() -> None:
    provider = FakeEmbeddingProvider(dimensions=8)
    document = asyncio.run(provider.embed_document(b"image"))
    query = asyncio.run(provider.embed_query(b"image"))

    assert document.vector != query.vector


def test_dimension_validation_rejects_invalid_provider_result() -> None:
    result = EmbeddingResult((0.1, 0.2), "broken", 2, EmbeddingUsage())

    with pytest.raises(ValueError, match="dimension mismatch"):
        result.validate_dimensions(3)


def test_fake_provider_rejects_empty_image() -> None:
    with pytest.raises(ValueError, match="image is required"):
        asyncio.run(FakeEmbeddingProvider(dimensions=8).embed_query(b""))
