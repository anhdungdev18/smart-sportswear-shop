class LlmClient:
    """Phase 5 keeps answer generation deterministic and grounded in tool output."""

    async def complete(self, prompt: str) -> str:
        return prompt
