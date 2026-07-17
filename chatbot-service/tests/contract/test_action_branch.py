"""
Contract tests for action branch tools (cancel_order, add_to_cart).

Tests the service layer's handling of backend HTTP responses
by patching backend_api_client at the HTTP call level.

Covered scenarios per tool (6 each):
  200 success | 401 auth | 404 not_found | 409 conflict | timeout | connect error
"""
from __future__ import annotations

import pytest
from unittest.mock import AsyncMock, patch

from app.clients.backend_api_client import BackendError
from app.schemas.action import CancelOrderResult, AddToCartResult


# ═══════════════════════════════════════════════════════════════════
# cancel_order (via order_action_service)
# ═══════════════════════════════════════════════════════════════════

@pytest.fixture
def mock_find_by_code():
    """Simulate backend returning a found order for code SP001."""
    return AsyncMock(return_value={"id": "uuid-001", "orderCode": "SP001", "orderStatus": "CONFIRMED"})


async def test_cancel_order_200(mock_find_by_code):
    cancelled_order = {
        "data": {"orderCode": "SP001", "orderStatus": "CANCELLED"}
    }
    with patch("app.clients.order_api_client.find_by_code", mock_find_by_code), \
         patch("app.clients.backend_api_client.post", new=AsyncMock(return_value=cancelled_order)):
        from app.services.order_action_service import cancel_order
        result = await cancel_order("SP001", access_token="tok1")

    assert result.success is True
    assert result.orderCode == "SP001"
    assert result.orderStatus == "CANCELLED"


async def test_cancel_order_401(mock_find_by_code):
    with patch("app.clients.order_api_client.find_by_code", mock_find_by_code), \
         patch("app.clients.backend_api_client.post",
               new=AsyncMock(side_effect=BackendError("Unauthorized", "auth_required", 401))):
        from app.services.order_action_service import cancel_order
        result = await cancel_order("SP001", access_token="expired_tok")

    assert result.success is False
    assert result.errorCode == "auth_required"


async def test_cancel_order_404():
    with patch("app.clients.order_api_client.find_by_code", new=AsyncMock(return_value=None)):
        from app.services.order_action_service import cancel_order
        result = await cancel_order("NONEXISTENT", access_token="tok1")

    assert result.success is False
    assert result.errorCode == "not_found"


async def test_cancel_order_409(mock_find_by_code):
    with patch("app.clients.order_api_client.find_by_code", mock_find_by_code), \
         patch("app.clients.backend_api_client.post",
               new=AsyncMock(side_effect=BackendError("Already cancelled", "conflict", 409))):
        from app.services.order_action_service import cancel_order
        result = await cancel_order("SP001", access_token="tok1")

    assert result.success is False
    assert result.errorCode == "conflict"


async def test_cancel_order_timeout(mock_find_by_code):
    with patch("app.clients.order_api_client.find_by_code", mock_find_by_code), \
         patch("app.clients.backend_api_client.post",
               new=AsyncMock(side_effect=BackendError("Backend timeout", "timeout"))):
        from app.services.order_action_service import cancel_order
        result = await cancel_order("SP001", access_token="tok1")

    assert result.success is False
    assert result.errorCode == "timeout"


async def test_cancel_order_connect_error(mock_find_by_code):
    with patch("app.clients.order_api_client.find_by_code", mock_find_by_code), \
         patch("app.clients.backend_api_client.post",
               new=AsyncMock(side_effect=BackendError("Backend unreachable", "unavailable"))):
        from app.services.order_action_service import cancel_order
        result = await cancel_order("SP001", access_token="tok1")

    assert result.success is False
    assert result.errorCode == "unavailable"


# ── cancel_order: no access token ─────────────────────────────────

async def test_cancel_order_no_token():
    from app.services.order_action_service import cancel_order
    result = await cancel_order("SP001", access_token=None)
    assert result.success is False
    assert result.errorCode == "auth_required"


# ═══════════════════════════════════════════════════════════════════
# add_to_cart (via cart_action_service)
# ═══════════════════════════════════════════════════════════════════

_CART_RESPONSE = {
    "data": {
        "id": "cart-1",
        "items": [{"variantId": "v1"}, {"variantId": "v2"}, {"variantId": "v3"}],
        "subtotal": 599000.0,
    }
}


async def test_add_to_cart_200():
    with patch("app.clients.backend_api_client.post",
               new=AsyncMock(return_value=_CART_RESPONSE)):
        from app.services.cart_action_service import add_to_cart
        result = await add_to_cart(
            variant_id="var-uuid",
            quantity=1,
            access_token="tok1",
        )

    assert result.success is True
    assert result.cartId == "cart-1"
    assert result.itemCount == 3


async def test_add_to_cart_401():
    with patch("app.clients.backend_api_client.post",
               new=AsyncMock(side_effect=BackendError("Unauthorized", "auth_required", 401))):
        from app.services.cart_action_service import add_to_cart
        result = await add_to_cart(variant_id="var-uuid", quantity=1, access_token="bad_tok")

    assert result.success is False
    assert result.errorCode == "auth_required"


async def test_add_to_cart_404():
    with patch("app.clients.backend_api_client.post",
               new=AsyncMock(side_effect=BackendError("Variant not found", "not_found", 404))):
        from app.services.cart_action_service import add_to_cart
        result = await add_to_cart(variant_id="ghost-uuid", quantity=1, access_token="tok1")

    assert result.success is False
    assert result.errorCode == "not_found"


async def test_add_to_cart_409():
    with patch("app.clients.backend_api_client.post",
               new=AsyncMock(side_effect=BackendError("Out of stock", "conflict", 409))):
        from app.services.cart_action_service import add_to_cart
        result = await add_to_cart(variant_id="var-uuid", quantity=99, access_token="tok1")

    assert result.success is False
    assert result.errorCode == "conflict"


async def test_add_to_cart_timeout():
    with patch("app.clients.backend_api_client.post",
               new=AsyncMock(side_effect=BackendError("Backend timeout", "timeout"))):
        from app.services.cart_action_service import add_to_cart
        result = await add_to_cart(variant_id="var-uuid", quantity=1, access_token="tok1")

    assert result.success is False
    assert result.errorCode == "timeout"


async def test_add_to_cart_connect_error():
    with patch("app.clients.backend_api_client.post",
               new=AsyncMock(side_effect=BackendError("Backend unreachable", "unavailable"))):
        from app.services.cart_action_service import add_to_cart
        result = await add_to_cart(variant_id="var-uuid", quantity=1, access_token="tok1")

    assert result.success is False
    assert result.errorCode == "unavailable"


# ── add_to_cart: no variant_id ────────────────────────────────────

async def test_add_to_cart_no_variant_id():
    from app.services.cart_action_service import add_to_cart
    result = await add_to_cart(variant_id="", quantity=1, access_token="tok1")
    assert result.success is False
