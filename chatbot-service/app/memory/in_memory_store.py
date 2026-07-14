from __future__ import annotations

from app.memory.base_store import BaseSessionStore, default_context


class InMemorySessionStore(BaseSessionStore):
    def __init__(self) -> None:
        self._data: dict[str, dict] = {}

    async def get(self, session_id: str) -> dict:
        if session_id not in self._data:
            self._data[session_id] = default_context()
        return dict(self._data[session_id])  # shallow copy

    async def save(self, session_id: str, ctx: dict) -> None:
        self._data[session_id] = dict(ctx)  # store shallow copy
