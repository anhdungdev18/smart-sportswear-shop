from fastapi.testclient import TestClient

from app.main import app
from tests.helpers import make_token


def test_chat_requires_valid_admin_jwt():
    client = TestClient(app)

    response = client.post("/chat", json={"sessionId": "s1", "message": "Ton kho co risk nao?"})

    assert response.status_code == 401


def test_chat_blocks_non_admin_jwt():
    client = TestClient(app)

    response = client.post(
        "/chat",
        json={"sessionId": "s1", "message": "Ton kho co risk nao?"},
        headers={"Authorization": f"Bearer {make_token('CUSTOMER')}"},
    )

    assert response.status_code == 403
