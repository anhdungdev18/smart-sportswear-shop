from __future__ import annotations

import json
from typing import Any

import httpx

from app.config.settings import settings


class LlmClient:
    """Optional LLM adapter.

    The service remains fully deterministic when MODEL_PROVIDER is "none" or no
    provider key is configured. Network/model failures deliberately fall back to
    deterministic routing and formatting in the graph.
    """

    def enabled(self) -> bool:
        provider = settings.MODEL_PROVIDER.lower()
        if provider == "openai":
            return bool(settings.OPENAI_API_KEY)
        if provider == "anthropic":
            return bool(settings.ANTHROPIC_API_KEY)
        return False

    async def complete(self, system_prompt: str, user_payload: dict[str, Any]) -> str:
        provider = settings.MODEL_PROVIDER.lower()
        if provider == "openai":
            return await self._complete_openai(system_prompt, user_payload)
        if provider == "anthropic":
            return await self._complete_anthropic(system_prompt, user_payload)
        raise RuntimeError("LLM provider is not configured")

    async def complete_json(self, system_prompt: str, user_payload: dict[str, Any]) -> dict[str, Any]:
        raw = await self.complete(system_prompt, user_payload)
        return _parse_json_object(raw)

    async def _complete_openai(self, system_prompt: str, user_payload: dict[str, Any]) -> str:
        async with httpx.AsyncClient(timeout=settings.LLM_TIMEOUT_SECONDS) as client:
            response = await client.post(
                "https://api.openai.com/v1/chat/completions",
                headers={"Authorization": f"Bearer {settings.OPENAI_API_KEY}"},
                json={
                    "model": settings.MODEL_NAME,
                    "temperature": 0,
                    "messages": [
                        {"role": "system", "content": system_prompt},
                        {"role": "user", "content": json.dumps(user_payload, ensure_ascii=False)},
                    ],
                    "response_format": {"type": "json_object"},
                },
            )
        response.raise_for_status()
        return str(response.json()["choices"][0]["message"]["content"])

    async def _complete_anthropic(self, system_prompt: str, user_payload: dict[str, Any]) -> str:
        async with httpx.AsyncClient(timeout=settings.LLM_TIMEOUT_SECONDS) as client:
            response = await client.post(
                "https://api.anthropic.com/v1/messages",
                headers={
                    "x-api-key": settings.ANTHROPIC_API_KEY,
                    "anthropic-version": "2023-06-01",
                },
                json={
                    "model": settings.MODEL_NAME,
                    "max_tokens": 800,
                    "temperature": 0,
                    "system": system_prompt,
                    "messages": [{"role": "user", "content": json.dumps(user_payload, ensure_ascii=False)}],
                },
            )
        response.raise_for_status()
        blocks = response.json().get("content", [])
        return "".join(str(block.get("text", "")) for block in blocks if isinstance(block, dict))


def _parse_json_object(raw: str) -> dict[str, Any]:
    try:
        parsed = json.loads(raw)
    except json.JSONDecodeError:
        start = raw.find("{")
        end = raw.rfind("}")
        if start < 0 or end <= start:
            raise
        parsed = json.loads(raw[start : end + 1])
    if not isinstance(parsed, dict):
        raise ValueError("LLM response was not a JSON object")
    return parsed
