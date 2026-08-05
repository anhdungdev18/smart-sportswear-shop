import httpx

from app.config import Settings

from .base import MultimodalEmbeddingProvider
from .fake import FakeEmbeddingProvider
from .voyage import VoyageEmbeddingProvider

_http_client: httpx.AsyncClient | None = None


def _shared_http_client(settings: Settings) -> httpx.AsyncClient:
    global _http_client
    if _http_client is None:
        timeout = httpx.Timeout(
            settings.voyage_timeout_seconds,
            connect=min(2.0, settings.voyage_timeout_seconds),
        )
        _http_client = httpx.AsyncClient(timeout=timeout)
    return _http_client


async def close_provider_clients() -> None:
    global _http_client
    if _http_client is not None:
        await _http_client.aclose()
        _http_client = None


def build_provider(settings: Settings) -> MultimodalEmbeddingProvider:
    if settings.visual_embedding_provider == "fake":
        return FakeEmbeddingProvider(settings.visual_embedding_dims)
    if settings.visual_embedding_provider == "voyage":
        return VoyageEmbeddingProvider(settings, client=_shared_http_client(settings))
    raise RuntimeError("Unsupported visual embedding provider")
