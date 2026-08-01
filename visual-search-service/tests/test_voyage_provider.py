import asyncio

import httpx
import pytest

from app.config import Settings
from app.providers.voyage import VoyageEmbeddingProvider, VoyageProviderError


def settings(**overrides) -> Settings:
    return Settings(
        voyage_api_key="test-secret",
        visual_embedding_provider="voyage",
        visual_embedding_dims=3,
        voyage_max_attempts=2,
        **overrides,
    )


def test_document_payload_and_usage_are_mapped_without_exposing_key():
    asyncio.run(_test_document_payload_and_usage_are_mapped_without_exposing_key())


async def _test_document_payload_and_usage_are_mapped_without_exposing_key():
    captured: dict = {}

    def handler(request: httpx.Request) -> httpx.Response:
        captured["authorization"] = request.headers["authorization"]
        captured["payload"] = __import__("json").loads(request.content)
        return httpx.Response(
            200,
            json={
                "data": [{"embedding": [0.1, 0.2, 0.3]}],
                "model": "voyage-multimodal-3.5",
                "usage": {"image_pixels": 100, "text_tokens": 2},
            },
        )

    async with httpx.AsyncClient(transport=httpx.MockTransport(handler)) as client:
        result = await VoyageEmbeddingProvider(settings(), client).embed_document(b"jpeg", "blue shoe")
    assert captured["authorization"] == "Bearer test-secret"
    assert captured["payload"]["input_type"] == "document"
    assert captured["payload"]["output_dimension"] == 3
    assert captured["payload"]["inputs"][0]["content"][1]["image_base64"].startswith("data:image/jpeg;base64,")
    assert result.usage.image_pixels == 100
    assert result.usage.text_tokens == 2


def test_query_uses_query_input_type_and_bounded_retry():
    asyncio.run(_test_query_uses_query_input_type_and_bounded_retry())


async def _test_query_uses_query_input_type_and_bounded_retry():
    calls = 0

    def handler(request: httpx.Request) -> httpx.Response:
        nonlocal calls
        calls += 1
        if calls == 1:
            return httpx.Response(429)
        return httpx.Response(200, json={"data": [{"embedding": [1, 0, 0]}], "usage": {}})

    async with httpx.AsyncClient(transport=httpx.MockTransport(handler)) as client:
        result = await VoyageEmbeddingProvider(settings(), client).embed_query(b"jpeg")
    assert calls == 2
    assert result.dimensions == 3


def test_dimension_mismatch_is_rejected():
    asyncio.run(_test_dimension_mismatch_is_rejected())


async def _test_dimension_mismatch_is_rejected():
    async with httpx.AsyncClient(
        transport=httpx.MockTransport(
            lambda _request: httpx.Response(200, json={"data": [{"embedding": [1, 2]}]})
        )
    ) as client:
        with pytest.raises(ValueError, match="dimension"):
            await VoyageEmbeddingProvider(settings(), client).embed_query(b"jpeg")


def test_non_retryable_provider_error_does_not_leak_body_or_secret():
    asyncio.run(_test_non_retryable_provider_error_does_not_leak_body_or_secret())


async def _test_non_retryable_provider_error_does_not_leak_body_or_secret():
    async with httpx.AsyncClient(
        transport=httpx.MockTransport(lambda _request: httpx.Response(400, text="secret response"))
    ) as client:
        with pytest.raises(VoyageProviderError) as caught:
            await VoyageEmbeddingProvider(settings(), client).embed_query(b"jpeg")
    assert "test-secret" not in str(caught.value)
    assert "secret response" not in str(caught.value)
