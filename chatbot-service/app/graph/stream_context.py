"""Per-request streaming sink.

When set, the response generator streams the final LLM answer token-by-token
into this queue instead of returning it in one blocking call. The SSE endpoint
drains the queue and forwards each delta to the browser. `None` (the default)
means the normal, non-streaming path.
"""
from __future__ import annotations

import asyncio
from contextvars import ContextVar

stream_sink: ContextVar[asyncio.Queue | None] = ContextVar("stream_sink", default=None)
