import asyncio
import base64

import httpx

from app.config import Settings

from .base import EmbeddingResult, EmbeddingUsage


class VoyageProviderError(RuntimeError):
    pass


class VoyageEmbeddingProvider:
    def __init__(self, settings: Settings, client: httpx.AsyncClient | None = None):
        if not settings.voyage_api_key:
            raise ValueError("VOYAGE_API_KEY is required when Voyage is selected")
        self.settings = settings
        self._client = client

    async def embed_document(self, image: bytes, text: str | None = None) -> EmbeddingResult:
        return await self._embed(image, "document", text)

    async def embed_query(self, image: bytes) -> EmbeddingResult:
        return await self._embed(image, "query", None)

    async def _embed(self, image: bytes, input_type: str, text: str | None) -> EmbeddingResult:
        content: list[dict[str, str]] = []
        if text:
            content.append({"type": "text", "text": text})
        encoded = base64.b64encode(image).decode("ascii")
        content.append({"type": "image_base64", "image_base64": f"data:image/jpeg;base64,{encoded}"})
        payload = {
            "inputs": [{"content": content}],
            "model": self.settings.visual_embedding_model,
            "input_type": input_type,
            "truncation": False,
            "output_dimension": self.settings.visual_embedding_dims,
        }
        client = self._client or httpx.AsyncClient(timeout=self.settings.voyage_timeout_seconds)
        owns_client = self._client is None
        try:
            for attempt in range(self.settings.voyage_max_attempts):
                try:
                    response = await client.post(
                        self.settings.voyage_api_url,
                        json=payload,
                        headers={"Authorization": f"Bearer {self.settings.voyage_api_key}"},
                    )
                except (httpx.TimeoutException, httpx.NetworkError) as exc:
                    if attempt + 1 == self.settings.voyage_max_attempts:
                        raise VoyageProviderError("Voyage request failed after bounded retries") from exc
                    await asyncio.sleep(0.1 * (2**attempt))
                    continue
                if response.status_code == 429 or response.status_code >= 500:
                    if attempt + 1 < self.settings.voyage_max_attempts:
                        await asyncio.sleep(0.1 * (2**attempt))
                        continue
                    raise VoyageProviderError(f"Voyage temporarily unavailable (HTTP {response.status_code})")
                if response.status_code >= 400:
                    raise VoyageProviderError(f"Voyage rejected the embedding request (HTTP {response.status_code})")
                body = response.json()
                try:
                    vector = tuple(float(value) for value in body["data"][0]["embedding"])
                    usage = body.get("usage") or {}
                except (KeyError, IndexError, TypeError, ValueError) as exc:
                    raise VoyageProviderError("Voyage returned an invalid response") from exc
                return EmbeddingResult(
                    vector=vector,
                    model=body.get("model", self.settings.visual_embedding_model),
                    dimensions=len(vector),
                    usage=EmbeddingUsage(
                        image_pixels=int(usage.get("image_pixels", 0)),
                        text_tokens=int(usage.get("text_tokens", 0)),
                    ),
                ).validate_dimensions(self.settings.visual_embedding_dims)
            raise VoyageProviderError("Voyage request failed")
        finally:
            if owns_client:
                await client.aclose()
