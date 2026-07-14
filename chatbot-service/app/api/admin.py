from __future__ import annotations

from fastapi import APIRouter
from pydantic import BaseModel
from app.policy import capability_policy
from app.tools.registry import registry

# NOTE: Phase 2 dev/mock only — no auth on these endpoints.
# Phase 6+: add admin auth guard before exposing to production.
router = APIRouter(prefix="/admin", tags=["admin (dev/mock)"])


class CapabilityUpdate(BaseModel):
    capability: str
    enabled: bool


@router.get("/capabilities")
def get_capabilities() -> dict:
    return {
        "capabilities": capability_policy.get_all(),
        "_note": "Phase 2 mock — in-memory only, resets on restart",
    }


@router.post("/capabilities")
def update_capability(body: CapabilityUpdate) -> dict:
    capability_policy.set_enabled(body.capability, body.enabled)
    return {"capability": body.capability, "enabled": body.enabled, "updated": True}


@router.get("/tools")
def list_tools() -> dict:
    return {"tools": registry.all_metadata()}
