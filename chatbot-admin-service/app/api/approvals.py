from __future__ import annotations

from fastapi import APIRouter, Header, Query

from app.auth.jwt_verifier import verify_admin_jwt
from app.schemas.approval import ApprovalDecisionRequest, ApprovalResponse, CreateApprovalRequest
from app.services.approval_service import (
    approve_approval,
    create_approval,
    execute_approval,
    list_approvals,
    reject_approval,
)

router = APIRouter(prefix="/approvals")


def _token_from_header(authorization: str | None) -> str | None:
    if authorization and authorization.lower().startswith("bearer "):
        return authorization[7:]
    return None


@router.get("", response_model=dict)
async def approvals(
    limit: int = Query(default=50, ge=1, le=100),
    status: str | None = Query(default=None),
    authorization: str | None = Header(default=None),
) -> dict:
    verify_admin_jwt(_token_from_header(authorization))
    return {"items": list_approvals(limit, status)}


@router.post("", response_model=ApprovalResponse)
async def request_approval(request: CreateApprovalRequest, authorization: str | None = Header(default=None)) -> ApprovalResponse:
    token = _token_from_header(authorization)
    actor = verify_admin_jwt(token)
    return await create_approval(request, actor, token or "")


@router.post("/{approval_id}/approve", response_model=ApprovalResponse)
async def approve(approval_id: str, request: ApprovalDecisionRequest, authorization: str | None = Header(default=None)) -> ApprovalResponse:
    actor = verify_admin_jwt(_token_from_header(authorization))
    return approve_approval(approval_id, actor, request.note)


@router.post("/{approval_id}/reject", response_model=ApprovalResponse)
async def reject(approval_id: str, request: ApprovalDecisionRequest, authorization: str | None = Header(default=None)) -> ApprovalResponse:
    actor = verify_admin_jwt(_token_from_header(authorization))
    return reject_approval(approval_id, actor, request.note)


@router.post("/{approval_id}/execute", response_model=ApprovalResponse)
async def execute(approval_id: str, authorization: str | None = Header(default=None)) -> ApprovalResponse:
    token = _token_from_header(authorization)
    actor = verify_admin_jwt(token)
    return await execute_approval(approval_id, actor, token or "")
