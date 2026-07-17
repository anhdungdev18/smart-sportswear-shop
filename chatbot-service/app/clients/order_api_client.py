from __future__ import annotations

from app.clients import backend_api_client as http
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_MY_ORDERS_PATH = "/api/v1/orders/me"
_CANCEL_PATH    = "/api/v1/orders/{order_id}/cancel"


async def get_my_orders(access_token: str, limit: int = 100) -> list[dict]:
    """
    GET /api/v1/orders/me — returns all orders for the authenticated user.
    `data` field is List<OrderResponse>.
    """
    resp = await http.get(_MY_ORDERS_PATH, access_token=access_token, params={"limit": limit})
    return resp.get("data") or []


async def find_by_code(access_token: str, order_code: str) -> dict | None:
    """
    Fetch user's orders and return the one matching order_code, or None.
    Backend has no filter-by-code endpoint, so we search client-side.
    """
    orders = await get_my_orders(access_token)
    for o in orders:
        if o.get("orderCode") == order_code:
            return o
    return None


async def cancel_order(access_token: str, order_id: str, reason: str | None = None) -> dict:
    """
    POST /api/v1/orders/{order_id}/cancel
    Returns OrderResponse inside ApiResponse.data.
    """
    path = _CANCEL_PATH.format(order_id=order_id)
    body = {"reason": reason} if reason else {}
    resp = await http.post(path, access_token=access_token, json=body)
    return resp.get("data") or {}
