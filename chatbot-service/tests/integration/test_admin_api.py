from __future__ import annotations

from datetime import datetime, timedelta, timezone

import jwt


_SECRET = "test-access-secret-that-is-at-least-32-bytes"


def _auth(role: str) -> dict[str, str]:
    token = jwt.encode(
        {
            "sub": "admin-api-test-user",
            "role": role,
            "type": "access",
            "exp": datetime.now(timezone.utc) + timedelta(minutes=5),
        },
        _SECRET,
        algorithm="HS256",
    )
    return {"Authorization": f"Bearer {token}"}


async def test_admin_api_requires_authentication(http_client):
    response = await http_client.get("/admin/tools")
    assert response.status_code == 401


async def test_admin_api_rejects_non_admin(http_client):
    response = await http_client.get("/admin/tools", headers=_auth("CUSTOMER"))
    assert response.status_code == 403


async def test_admin_can_read_and_update_capabilities(http_client):
    headers = _auth("ADMIN")
    response = await http_client.get("/admin/tools", headers=headers)
    assert response.status_code == 200
    assert response.json()["tools"]

    updated = await http_client.post(
        "/admin/capabilities",
        headers=headers,
        json={"capability": "knowledge_qa", "enabled": True},
    )
    assert updated.status_code == 200
    assert updated.json()["updated"] is True
