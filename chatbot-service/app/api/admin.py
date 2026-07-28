from __future__ import annotations

import secrets

from fastapi import APIRouter, Depends, Header, HTTPException, status
from pydantic import BaseModel

from app.policy import capability_policy
from app.services.auth_identity import verify_access_token
from app.tools.registry import registry

# Capability state is in memory, but every endpoint requires a verified ADMIN JWT.
router = APIRouter(prefix="/admin", tags=["admin"])


def require_admin(authorization: str | None = Header(default=None)) -> None:
    scheme, _, token = (authorization or "").partition(" ")
    if not secrets.compare_digest(scheme.lower(), "bearer"):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Authentication required")
    identity = verify_access_token(token)
    if identity is None:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid access token")
    if identity.role != "ADMIN":
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Admin access required")


class CapabilityUpdate(BaseModel):
    capability: str
    enabled: bool


@router.get("/capabilities", dependencies=[Depends(require_admin)])
def get_capabilities() -> dict:
    return {
        "capabilities": capability_policy.get_all(),
        "_note": "In-memory only; resets on restart",
    }


@router.post("/capabilities", dependencies=[Depends(require_admin)])
def update_capability(body: CapabilityUpdate) -> dict:
    capability_policy.set_enabled(body.capability, body.enabled)
    return {"capability": body.capability, "enabled": body.enabled, "updated": True}


@router.get("/tools", dependencies=[Depends(require_admin)])
def list_tools() -> dict:
    return {"tools": registry.all_metadata()}
