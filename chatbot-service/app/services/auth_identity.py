from __future__ import annotations

from dataclasses import dataclass

import jwt

from app.config.settings import settings


@dataclass(frozen=True)
class AuthIdentity:
    user_id: str
    role: str


def verify_access_token(access_token: str | None) -> AuthIdentity | None:
    """Verify a Spring Boot access token and return claims trusted by the chatbot."""
    if not access_token or not settings.JWT_ACCESS_SECRET:
        return None
    try:
        claims = jwt.decode(
            access_token,
            settings.JWT_ACCESS_SECRET,
            algorithms=["HS256"],
            options={"require": ["sub", "exp", "type", "role"]},
        )
        if claims.get("type") != "access":
            return None
        user_id = str(claims["sub"]).strip()
        role = str(claims["role"]).strip().upper()
        if not user_id or not role:
            return None
        return AuthIdentity(user_id=user_id, role=role)
    except (jwt.PyJWTError, KeyError, TypeError, ValueError):
        return None


def session_storage_id(public_session_id: str, identity: AuthIdentity | None) -> str:
    """Prevent two authenticated users from sharing context through the same client session ID."""
    if identity is None:
        return f"guest:{public_session_id}"
    return f"user:{identity.user_id}:{public_session_id}"
