from __future__ import annotations

from fastapi.testclient import TestClient

from app.config.settings import settings
from app.main import app
from app.services import approval_service
from tests.helpers import make_token


class FakeForecastingClient:
    async def accept_replenishment(self, token: str, recommendation_id: str, payload: dict):
        return {"ok": True}

    async def adjust_replenishment(self, token: str, recommendation_id: str, payload: dict):
        return {"ok": True}

    async def dismiss_replenishment(self, token: str, recommendation_id: str, payload: dict):
        return {"ok": True}


def setup_function():
    approval_service.clear_approvals_for_tests()


def test_create_approval_requires_enabled_workflow(monkeypatch):
    monkeypatch.setattr(settings, "APPROVALS_ENABLED", False)
    client = TestClient(app)

    response = client.post(
        "/approvals",
        json={
            "action": "ACCEPT_REPLENISHMENT",
            "resourceId": "rec-1",
            "payload": {"note": "ok"},
            "idempotencyKey": "idem-approval-1",
            "reason": "accept recommendation",
        },
        headers={"Authorization": f"Bearer {make_token('ADMIN')}"},
    )

    assert response.status_code == 403


def test_approval_idempotency_and_execute_gate(monkeypatch):
    monkeypatch.setattr(settings, "APPROVALS_ENABLED", True)
    monkeypatch.setattr(settings, "WRITE_TOOLS_ENABLED", False)

    async def fake_snapshot(token: str, recommendation_id: str):
        return {"id": recommendation_id, "variantId": "variant-1", "status": "PENDING", "suggestedQuantity": 30}

    monkeypatch.setattr(approval_service, "_fetch_replenishment_snapshot", fake_snapshot)
    client = TestClient(app)
    headers = {"Authorization": f"Bearer {make_token('ADMIN')}"}
    body = {
        "action": "ACCEPT_REPLENISHMENT",
        "resourceId": "rec-1",
        "payload": {"note": "accept via approval"},
        "idempotencyKey": "idem-approval-2",
        "reason": "stockout risk is high",
    }

    first = client.post("/approvals", json=body, headers=headers)
    second = client.post("/approvals", json=body, headers=headers)

    assert first.status_code == 200
    assert second.status_code == 200
    assert first.json()["id"] == second.json()["id"]
    assert first.json()["payloadHash"] == second.json()["payloadHash"]

    approval_id = first.json()["id"]
    approved = client.post(f"/approvals/{approval_id}/approve", json={"note": "reviewed"}, headers=headers)
    blocked = client.post(f"/approvals/{approval_id}/execute", headers=headers)

    assert approved.status_code == 200
    assert approved.json()["status"] == "APPROVED"
    assert blocked.status_code == 403


def test_execute_approval_records_after_snapshot_and_audit(monkeypatch):
    monkeypatch.setattr(settings, "APPROVALS_ENABLED", True)
    monkeypatch.setattr(settings, "WRITE_TOOLS_ENABLED", True)
    snapshots = [
        {"id": "rec-2", "variantId": "variant-2", "status": "PENDING", "suggestedQuantity": 20},
        {"id": "rec-2", "variantId": "variant-2", "status": "PENDING", "suggestedQuantity": 20},
        {"id": "rec-2", "variantId": "variant-2", "status": "ACCEPTED", "adminQuantity": 20},
    ]

    async def fake_snapshot(token: str, recommendation_id: str):
        return snapshots.pop(0)

    monkeypatch.setattr(approval_service, "_fetch_replenishment_snapshot", fake_snapshot)
    monkeypatch.setattr(approval_service, "ForecastingClient", FakeForecastingClient)
    client = TestClient(app)
    headers = {"Authorization": f"Bearer {make_token('ADMIN')}"}

    created = client.post(
        "/approvals",
        json={
            "action": "ACCEPT_REPLENISHMENT",
            "resourceId": "rec-2",
            "payload": {"note": "accept via approval"},
            "idempotencyKey": "idem-approval-3",
            "reason": "stockout risk is high",
        },
        headers=headers,
    )
    approval_id = created.json()["id"]
    client.post(f"/approvals/{approval_id}/approve", json={}, headers=headers)
    executed = client.post(f"/approvals/{approval_id}/execute", headers=headers)

    assert executed.status_code == 200
    payload = executed.json()
    assert payload["status"] == "EXECUTED"
    assert payload["beforeSnapshot"]["status"] == "PENDING"
    assert payload["afterSnapshot"]["status"] == "ACCEPTED"
    assert [entry["event"] for entry in payload["audit"]] == ["REQUESTED", "APPROVED", "EXECUTED"]
