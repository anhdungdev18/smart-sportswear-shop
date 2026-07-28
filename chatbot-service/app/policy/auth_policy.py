from __future__ import annotations

# The API layer verifies the JWT and only forwards the token when it is valid.


def is_authenticated(access_token: str | None) -> bool:
    return access_token is not None and access_token.strip() != ""


def check(requires_auth: bool, access_token: str | None) -> tuple[bool, str]:
    """Return whether execution is allowed and the corresponding policy reason."""
    if not requires_auth:
        return True, "no_auth_required"
    if is_authenticated(access_token):
        return True, "authenticated"
    return False, "auth_required"
