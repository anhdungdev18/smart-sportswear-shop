from __future__ import annotations

import base64
import hashlib
import hmac
import json
import time
from fastapi import HTTPException, status

from app.auth.actor_context import ActorContext
from app.config.settings import settings


def _normalize_role(payload: dict) -> str:
    raw_role = payload.get("role") or payload.get("userRole") or payload.get("authorities") or ""
    if isinstance(raw_role, list):
        roles = [str(role).replace("ROLE_", "").upper() for role in raw_role]
        return "ADMIN" if "ADMIN" in roles else (roles[0] if roles else "")
    return str(raw_role).replace("ROLE_", "").upper()


def _b64url_decode(value: str) -> bytes:
    padding = "=" * (-len(value) % 4)
    return base64.urlsafe_b64decode(value + padding)


def _decode_hs256(token: str) -> dict:
    parts = token.split(".")
    if len(parts) != 3:
        raise ValueError("JWT must have three parts")
    signing_input = f"{parts[0]}.{parts[1]}".encode("ascii")
    expected = hmac.new(settings.JWT_ACCESS_SECRET.encode("utf-8"), signing_input, hashlib.sha256).digest()
    actual = _b64url_decode(parts[2])
    if not hmac.compare_digest(expected, actual):
        raise ValueError("JWT signature mismatch")
    header = json.loads(_b64url_decode(parts[0]))
    if header.get("alg") != settings.JWT_ACCESS_ALGORITHM:
        raise ValueError("Unsupported JWT algorithm")
    payload = json.loads(_b64url_decode(parts[1]))
    if "exp" not in payload or float(payload["exp"]) < time.time():
        raise ValueError("JWT expired or missing exp")
    return payload


def verify_admin_jwt(token: str | None) -> ActorContext:
    if not token:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing access token")
    try:
        payload = _decode_hs256(token)
    except Exception as exc:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid access token") from exc

    role = _normalize_role(payload)
    subject = str(payload.get("sub") or "")
    actor_id = str(payload.get("userId") or payload.get("id") or subject)
    if not actor_id or not role:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Token missing actor claims")
    return ActorContext(actor_id=actor_id, role=role, subject=subject)
