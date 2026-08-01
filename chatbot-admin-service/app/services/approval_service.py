from __future__ import annotations

import hashlib
import json
from datetime import datetime, timedelta, timezone
from typing import Any
from uuid import uuid4

from fastapi import HTTPException, status

from app.auth.actor_context import ActorContext
from app.clients.forecasting_client import ForecastingClient
from app.config.settings import settings
from app.schemas.approval import ApprovalResponse, CreateApprovalRequest
from app.services.approval_store import approval_store


def list_approvals(limit: int = 50, status_filter: str | None = None) -> list[ApprovalResponse]:
    return [ApprovalResponse(**row) for row in approval_store.list(limit, status_filter)]


async def create_approval(request: CreateApprovalRequest, actor: ActorContext, token: str) -> ApprovalResponse:
    if not settings.APPROVALS_ENABLED:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Approval workflow is disabled")
    if request.action not in {"ACCEPT_REPLENISHMENT", "ADJUST_REPLENISHMENT", "DISMISS_REPLENISHMENT"}:
        raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail="Unsupported write action")
    if request.action == "ADJUST_REPLENISHMENT":
        quantity = request.payload.get("quantity")
        if not isinstance(quantity, int) or quantity < 0:
            raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail="Adjust quantity is required")
    if request.action == "DISMISS_REPLENISHMENT" and not str(request.payload.get("note", "")).strip():
        raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail="Dismiss note is required")

    existing = approval_store.get_by_idempotency_key(request.idempotencyKey)
    if existing:
        return ApprovalResponse(**existing)

    before = await _fetch_replenishment_snapshot(token, request.resourceId)
    if before.get("status") != "PENDING":
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Resource is no longer pending")

    now = _now()
    row = {
        "id": str(uuid4()),
        "action": request.action,
        "resourceType": request.resourceType,
        "resourceId": request.resourceId,
        "payload": _canonical_payload(request.payload),
        "payloadHash": _hash_payload(request.payload),
        "idempotencyKey": request.idempotencyKey,
        "reason": request.reason,
        "riskLevel": request.riskLevel,
        "status": "PENDING",
        "requestedBy": actor.actor_id,
        "approvedBy": None,
        "executedBy": None,
        "beforeSnapshot": before,
        "afterSnapshot": None,
        "audit": [_audit("REQUESTED", actor, {"payloadHash": _hash_payload(request.payload)})],
        "error": None,
        "expiresAt": (datetime.now(timezone.utc) + timedelta(minutes=30)).isoformat(),
        "createdAt": now,
        "updatedAt": now,
    }
    return ApprovalResponse(**approval_store.save(row))


def approve_approval(approval_id: str, actor: ActorContext, note: str | None = None) -> ApprovalResponse:
    row = _get_approval(approval_id)
    _assert_pending(row)
    row["status"] = "APPROVED"
    row["approvedBy"] = actor.actor_id
    row["audit"].append(_audit("APPROVED", actor, {"note": note}))
    row["updatedAt"] = _now()
    return ApprovalResponse(**approval_store.save(row))


def reject_approval(approval_id: str, actor: ActorContext, note: str | None = None) -> ApprovalResponse:
    row = _get_approval(approval_id)
    _assert_pending(row)
    row["status"] = "REJECTED"
    row["approvedBy"] = actor.actor_id
    row["audit"].append(_audit("REJECTED", actor, {"note": note}))
    row["updatedAt"] = _now()
    return ApprovalResponse(**approval_store.save(row))


async def execute_approval(approval_id: str, actor: ActorContext, token: str) -> ApprovalResponse:
    if not settings.WRITE_TOOLS_ENABLED:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Write tools are disabled")
    row = _get_approval(approval_id)
    executed = approval_store.execution_result(row["idempotencyKey"])
    if executed:
        return ApprovalResponse(**executed)
    if row["status"] != "APPROVED":
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Approval is not approved")
    _assert_not_expired(row)

    current = await _fetch_replenishment_snapshot(token, row["resourceId"])
    if current.get("status") != "PENDING":
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Resource changed before execution")
    if current.get("variantId") != row["beforeSnapshot"].get("variantId"):
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Resource identity changed before execution")

    client = ForecastingClient()
    try:
        if row["action"] == "ACCEPT_REPLENISHMENT":
            await client.accept_replenishment(token, row["resourceId"], {"note": row["payload"].get("note")})
        elif row["action"] == "ADJUST_REPLENISHMENT":
            await client.adjust_replenishment(token, row["resourceId"], row["payload"])
        elif row["action"] == "DISMISS_REPLENISHMENT":
            await client.dismiss_replenishment(token, row["resourceId"], row["payload"])
        else:
            raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail="Unsupported write action")
        row["afterSnapshot"] = await _fetch_replenishment_snapshot(token, row["resourceId"])
        row["status"] = "EXECUTED"
        row["executedBy"] = actor.actor_id
        row["audit"].append(_audit("EXECUTED", actor, {"afterStatus": row["afterSnapshot"].get("status")}))
        row["updatedAt"] = _now()
        return ApprovalResponse(**approval_store.save(row))
    except Exception as exc:
        row["status"] = "FAILED"
        row["error"] = str(exc)
        row["audit"].append(_audit("FAILED", actor, {"error": str(exc)}))
        row["updatedAt"] = _now()
        approval_store.save(row)
        raise


def clear_approvals_for_tests() -> None:
    approval_store.clear_for_tests()


async def _fetch_replenishment_snapshot(token: str, recommendation_id: str) -> dict[str, Any]:
    return await ForecastingClient().replenishment_detail(token, recommendation_id)


def _get_approval(approval_id: str) -> dict[str, Any]:
    row = approval_store.get(approval_id)
    if not row:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Approval not found")
    return row


def _assert_pending(row: dict[str, Any]) -> None:
    _assert_not_expired(row)
    if row["status"] != "PENDING":
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Approval is not pending")


def _assert_not_expired(row: dict[str, Any]) -> None:
    if datetime.fromisoformat(row["expiresAt"]) <= datetime.now(timezone.utc):
        row["status"] = "EXPIRED"
        row["updatedAt"] = _now()
        approval_store.save(row)
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Approval expired")


def _canonical_payload(payload: dict[str, Any]) -> dict[str, Any]:
    return json.loads(json.dumps(payload, sort_keys=True, separators=(",", ":"), default=str))


def _hash_payload(payload: dict[str, Any]) -> str:
    raw = json.dumps(_canonical_payload(payload), sort_keys=True, separators=(",", ":")).encode("utf-8")
    return hashlib.sha256(raw).hexdigest()


def _audit(event: str, actor: ActorContext, extra: dict[str, Any] | None = None) -> dict[str, Any]:
    return {
        "event": event,
        "actorId": actor.actor_id,
        "role": actor.role,
        "at": _now(),
        "extra": extra or {},
    }


def _now() -> str:
    return datetime.now(timezone.utc).isoformat()
