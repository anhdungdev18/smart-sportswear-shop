from fastapi.testclient import TestClient

from app.main import app


def test_liveness() -> None:
    response = TestClient(app).get("/health/live")

    assert response.status_code == 200
    assert response.json() == {"status": "ok", "service": "visual-search-service"}


def test_readiness_is_disabled_by_default() -> None:
    response = TestClient(app).get("/health/ready")

    assert response.status_code == 200
    assert response.json()["enabled"] is False
