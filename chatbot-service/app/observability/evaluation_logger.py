from __future__ import annotations

import json
from datetime import datetime, timezone

from app.observability.trace_logger import get_logger

logger = get_logger("chatbot.evaluation")


def log_turn(
    *,
    session_id: str,
    user_id: str | None,
    query: str,
    intent: str,
    tool: str,
    latency_ms: float,
    success: bool,
    reply: str,
    retrieval_branch: str = "",
    pending_event: str = "",
) -> None:
    """
    One structured record per conversation turn.
    Fields are designed to support offline quality evaluation.
    Never logs access_token or any secret.
    """
    try:
        record = {
            "ts": datetime.now(timezone.utc).isoformat(),
            "session_id": session_id,
            "user_id": user_id,
            "query": query,
            "intent": intent,
            "tool": tool,
            "latency_ms": round(latency_ms, 1),
            "success": success,
            "reply_len": len(reply),
            "retrieval_branch": retrieval_branch,
            "pending_event": pending_event,
        }
        logger.info(json.dumps(record, ensure_ascii=False, default=str))
    except Exception:
        logger.warning("evaluation_logger.log_turn failed — skipped")
