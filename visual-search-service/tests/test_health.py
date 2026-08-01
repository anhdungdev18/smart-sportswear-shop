from fastapi.testclient import TestClient
from unittest.mock import AsyncMock, patch

from app.config import Settings, get_settings
from app.main import app
from app.services.readiness import ReadinessResult


def test_liveness() -> None:
    response = TestClient(app).get("/health/live")

    assert response.status_code == 200
    assert response.json() == {"status": "ok", "service": "visual-search-service"}


def test_readiness_is_disabled_by_default() -> None:
    app.dependency_overrides[get_settings] = lambda: Settings(_env_file=None)
    try:
        response = TestClient(app).get("/health/ready")
    finally:
        app.dependency_overrides.clear()

    assert response.status_code == 200
    assert response.json()["enabled"] is False


def test_readiness_checks_dependencies_when_enabled() -> None:
    app.dependency_overrides[get_settings] = lambda: Settings(
        _env_file=None,
        visual_search_enabled=True,
        database_url="postgresql://configured",
        rabbitmq_url="amqp://configured",
        internal_service_token="configured",
        cloudinary_cloud_name="demo",
    )
    try:
        with patch(
            "app.api.health.ReadinessChecker.check",
            new=AsyncMock(return_value=ReadinessResult(True, "up", "active", "up")),
        ):
            response = TestClient(app).get("/health/ready")
    finally:
        app.dependency_overrides.clear()
    assert response.status_code == 200
    assert response.json()["status"] == "ready"


def test_readiness_returns_503_when_a_dependency_is_down() -> None:
    app.dependency_overrides[get_settings] = lambda: Settings(
        _env_file=None,
        visual_search_enabled=True,
        database_url="postgresql://configured",
        rabbitmq_url="amqp://configured",
        internal_service_token="configured",
        cloudinary_cloud_name="demo",
    )
    try:
        with patch(
            "app.api.health.ReadinessChecker.check",
            new=AsyncMock(return_value=ReadinessResult(False, "up", "active", "down")),
        ):
            response = TestClient(app).get("/health/ready")
    finally:
        app.dependency_overrides.clear()
    assert response.status_code == 503
    assert response.json()["status"] == "not_ready"
    assert response.json()["checks"]["rabbitmq"] == "down"
