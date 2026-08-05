from fastapi import FastAPI
from fastapi.testclient import TestClient

from app.api import internal_product_search


def test_internal_token_and_response(monkeypatch):
    app = FastAPI()
    app.include_router(internal_product_search.router)
    monkeypatch.setattr(internal_product_search.settings, "PRODUCT_SEARCH_INTERNAL_TOKEN", "test-secret")

    async def fake_search(_request):
        return {
            "items": [],
            "total": 0,
            "parsedQuery": {"normalized": "giay", "semanticText": "giay"},
            "searchMode": "KEYWORD",
            "processingTimeMs": 1,
        }

    monkeypatch.setattr(internal_product_search, "search_internal", fake_search)
    client = TestClient(app)
    payload = {"query": "giày", "page": 1, "limit": 20, "filters": {}}
    assert client.post("/internal/v1/product-search", json=payload).status_code == 401
    response = client.post(
        "/internal/v1/product-search",
        json=payload,
        headers={"X-Internal-Token": "test-secret"},
    )
    assert response.status_code == 200
    assert response.json()["searchMode"] == "KEYWORD"


def test_internal_input_validation(monkeypatch):
    app = FastAPI()
    app.include_router(internal_product_search.router)
    monkeypatch.setattr(internal_product_search.settings, "PRODUCT_SEARCH_INTERNAL_TOKEN", "test-secret")
    response = TestClient(app).post(
        "/internal/v1/product-search",
        json={"query": "x", "page": 0, "limit": 101, "filters": {}},
        headers={"X-Internal-Token": "test-secret"},
    )
    assert response.status_code == 422
