from __future__ import annotations

# Phase 2: auth is derived from ToolDefinition.requires_auth + userId presence.
# Phase 5+: replace with JWT validation / session lookup.


def is_authenticated(user_id: str | None) -> bool:
    """True if request carries a non-empty userId."""
    return user_id is not None and user_id.strip() != ""


def check(requires_auth: bool, user_id: str | None) -> tuple[bool, str]:
    """
    Returns (passed, reason).
    passed=True  → auth check OK
    passed=False → block with reason
    """
    if not requires_auth:
        return True, "no_auth_required"
    if is_authenticated(user_id):
        return True, "authenticated"
    return False, "auth_required"
