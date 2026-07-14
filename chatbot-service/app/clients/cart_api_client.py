from __future__ import annotations

from app.clients import backend_api_client as http
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_ADD_ITEM_PATH = "/api/v1/cart/items"


async def add_item(access_token: str, variant_id: str, quantity: int) -> dict:
    """
    POST /api/v1/cart/items — { variantId, quantity }
    Backend requires variantId as UUID string.
    Returns CartResponse inside ApiResponse.data.
    """
    body = {"variantId": variant_id, "quantity": quantity}
    resp = await http.post(_ADD_ITEM_PATH, access_token=access_token, json=body)
    return resp.get("data") or {}
