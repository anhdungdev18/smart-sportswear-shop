from __future__ import annotations

from collections import deque
from typing import Any

_RUNS: deque[dict[str, Any]] = deque(maxlen=500)
_FEEDBACK: deque[dict[str, Any]] = deque(maxlen=500)


def save_run(run: dict[str, Any]) -> None:
    _RUNS.appendleft(run)


def list_runs(limit: int = 50) -> list[dict[str, Any]]:
    return list(_RUNS)[:limit]


def save_feedback(feedback: dict[str, Any]) -> None:
    _FEEDBACK.appendleft(feedback)


def list_feedback(limit: int = 50) -> list[dict[str, Any]]:
    return list(_FEEDBACK)[:limit]
