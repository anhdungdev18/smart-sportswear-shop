from __future__ import annotations

from collections import deque
from typing import Any

_RUNS: deque[dict[str, Any]] = deque(maxlen=500)


def save_run(run: dict[str, Any]) -> None:
    _RUNS.appendleft(run)


def list_runs(limit: int = 50) -> list[dict[str, Any]]:
    return list(_RUNS)[:limit]
