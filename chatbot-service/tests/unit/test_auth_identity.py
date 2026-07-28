from __future__ import annotations

from datetime import datetime, timedelta, timezone

import jwt

from app.services.auth_identity import session_storage_id, verify_access_token


_SECRET = "test-access-secret-that-is-at-least-32-bytes"


def _token(*, user_id: str = "u1", role: str = "CUSTOMER", token_type: str = "access") -> str:
    return jwt.encode(
        {
            "sub": user_id,
            "role": role,
            "type": token_type,
            "exp": datetime.now(timezone.utc) + timedelta(minutes=5),
        },
        _SECRET,
        algorithm="HS256",
    )


def test_valid_access_token_returns_trusted_identity():
    identity = verify_access_token(_token(role="ADMIN"))
    assert identity is not None
    assert identity.user_id == "u1"
    assert identity.role == "ADMIN"


def test_invalid_or_refresh_token_is_rejected():
    assert verify_access_token("not-a-jwt") is None
    assert verify_access_token(_token(token_type="refresh")) is None


def test_authenticated_session_is_namespaced_by_verified_user():
    first = verify_access_token(_token(user_id="u1"))
    second = verify_access_token(_token(user_id="u2"))
    assert session_storage_id("same", first) != session_storage_id("same", second)
