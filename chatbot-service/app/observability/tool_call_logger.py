from __future__ import annotations

import json

from app.observability.trace_logger import get_logger

logger = get_logger("chatbot.tools")


def log_tool_call(
    session_id: str,
    tool_name: str,
    args: dict,
    result: dict | None = None,
    latency_ms: float | None = None,
    success: bool = True,
) -> None:
    """
    Structured log for every tool dispatch.
    access_token is never included in args.
    """
    try:
        safe_args = {k: v for k, v in args.items() if k != "access_token"}
        record: dict = {
            "session_id": session_id,
            "tool": tool_name,
            "args": safe_args,
            "success": success,
        }
        if latency_ms is not None:
            record["latency_ms"] = round(latency_ms, 1)
        logger.debug(json.dumps(record, ensure_ascii=False, default=str))
    except Exception:
        logger.warning(f"tool_call_logger.log_tool_call failed for tool={tool_name} — skipped")


def log_pending_action(
    session_id: str,
    event: str,   # "created" | "confirmed" | "rejected" | "expired" | "missing"
    tool: str,
    display: str = "",
) -> None:
    """Log lifecycle events of the confirmation pending-action."""
    logger.info(
        f"[{session_id}] pending_action | event={event} tool={tool} display={display!r}"
    )
