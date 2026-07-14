"""Provider-aware async LLM client used by generation, rewrite and tool selection."""
from __future__ import annotations

import asyncio
import json

from app.config.settings import settings
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)
_MAX_RETRIES = 2


def provider_name() -> str:
    return settings.MODEL_PROVIDER.strip().lower()


def is_available() -> bool:
    provider = provider_name()
    if provider == "anthropic":
        return bool(settings.ANTHROPIC_API_KEY and settings.MODEL_NAME)
    if provider == "openai":
        return bool(settings.OPENAI_API_KEY and settings.MODEL_NAME)
    return False


def _anthropic_messages(messages: list[dict]) -> tuple[str, list[dict]]:
    system_parts = [m["content"] for m in messages if m.get("role") == "system"]
    conversation = [
        {"role": m["role"], "content": m["content"]}
        for m in messages
        if m.get("role") in ("user", "assistant")
    ]
    return "\n\n".join(system_parts), conversation


async def _complete_once(
    messages: list[dict], temperature: float, max_tokens: int
) -> str | None:
    provider = provider_name()
    if provider == "anthropic":
        from anthropic import AsyncAnthropic

        system, conversation = _anthropic_messages(messages)
        client = AsyncAnthropic(api_key=settings.ANTHROPIC_API_KEY)
        response = await client.messages.create(
            model=settings.MODEL_NAME,
            system=system,
            messages=conversation,
            temperature=temperature,
            max_tokens=max_tokens,
        )
        text_blocks = [block.text for block in response.content if block.type == "text"]
        return "\n".join(text_blocks).strip() or None

    if provider == "openai":
        from openai import AsyncOpenAI

        client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY)
        response = await client.chat.completions.create(
            model=settings.MODEL_NAME,
            messages=messages,
            temperature=temperature,
            max_tokens=max_tokens,
        )
        return response.choices[0].message.content

    raise ValueError(f"Unsupported MODEL_PROVIDER: {settings.MODEL_PROVIDER}")


async def chat_complete(
    messages: list[dict], *, temperature: float = 0.4, max_tokens: int = 600
) -> str | None:
    if not is_available():
        logger.debug("llm_client | provider unavailable — skipping LLM call")
        return None

    for attempt in range(_MAX_RETRIES + 1):
        try:
            content = await _complete_once(messages, temperature, max_tokens)
            logger.debug(f"llm_client | provider={provider_name()} model={settings.MODEL_NAME}")
            return content
        except Exception as exc:
            logger.warning(f"llm_client | attempt={attempt + 1}/{_MAX_RETRIES + 1} error={exc!r}")
            if attempt < _MAX_RETRIES:
                await asyncio.sleep(1.5 * (attempt + 1))
    return None


async def select_tool(
    system_prompt: str,
    message: str,
    tools: list[dict],
) -> tuple[str, dict] | None:
    """Select one tool using the configured provider; returns (name, arguments)."""
    if not is_available():
        return None

    provider = provider_name()
    try:
        if provider == "anthropic":
            from anthropic import AsyncAnthropic

            anthropic_tools = [
                {
                    "name": tool["function"]["name"],
                    "description": tool["function"]["description"],
                    "input_schema": tool["function"]["parameters"],
                }
                for tool in tools
            ]
            client = AsyncAnthropic(api_key=settings.ANTHROPIC_API_KEY)
            response = await client.messages.create(
                model=settings.MODEL_NAME,
                system=system_prompt,
                messages=[{"role": "user", "content": message}],
                tools=anthropic_tools,
                tool_choice={"type": "auto"},
                temperature=0,
                max_tokens=150,
            )
            block = next((b for b in response.content if b.type == "tool_use"), None)
            return (block.name, dict(block.input)) if block else None

        if provider == "openai":
            from openai import AsyncOpenAI

            client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY)
            response = await client.chat.completions.create(
                model=settings.MODEL_NAME,
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": message},
                ],
                tools=tools,
                tool_choice="auto",
                temperature=0,
                max_tokens=150,
            )
            calls = response.choices[0].message.tool_calls
            if not calls:
                return None
            call = calls[0]
            return call.function.name, json.loads(call.function.arguments or "{}")
    except Exception as exc:
        logger.warning(f"llm_client | tool_selection_error={exc!r}")
    return None
