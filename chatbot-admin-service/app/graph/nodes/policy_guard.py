from fastapi import HTTPException, status

from app.auth.actor_context import ActorContext
from app.config.settings import settings
from app.policy.capability_policy import assert_read_only_tool
from app.policy.limits import assert_rate_limit
from app.policy.role_policy import assert_can_use_admin_copilot


def guard_policy(actor: ActorContext, tool_name: str) -> None:
    try:
        assert_can_use_admin_copilot(actor)
        assert_rate_limit(actor.actor_id)
        assert_read_only_tool(tool_name)
    except PermissionError as exc:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail=str(exc)) from exc
    if not settings.READ_ONLY_MODE or settings.WRITE_TOOLS_ENABLED:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Admin Copilot write mode is disabled in Phase 5")
