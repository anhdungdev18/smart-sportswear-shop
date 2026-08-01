from app.config import Settings

from .base import MultimodalEmbeddingProvider
from .fake import FakeEmbeddingProvider
from .voyage import VoyageEmbeddingProvider


def build_provider(settings: Settings) -> MultimodalEmbeddingProvider:
    if settings.visual_embedding_provider == "fake":
        return FakeEmbeddingProvider(settings.visual_embedding_dims)
    if settings.visual_embedding_provider == "voyage":
        return VoyageEmbeddingProvider(settings)
    raise RuntimeError("Unsupported visual embedding provider")
