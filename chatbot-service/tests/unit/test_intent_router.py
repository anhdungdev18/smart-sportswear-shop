"""Unit tests for graph/nodes/intent_router.py"""
from __future__ import annotations

import pytest
from datetime import datetime, timezone, timedelta

from app.graph.nodes.intent_router import intent_router_node, detect_intent
from app.memory.base_store import PENDING_ACTION_TTL_SECONDS


def _state(message: str, pending: str | None = None, created_at: str | None = None) -> dict:
    ctx = {}
    if pending:
        ctx["pending_action"] = pending
        ctx["pending_action_created_at"] = created_at
        ctx["pending_action_payload"] = {}
    return {
        "session_id": "test_sess",
        "message": message,
        "session_context": ctx,
        "access_token": None,
    }


def _fresh_ts() -> str:
    return datetime.now(timezone.utc).isoformat()


def _expired_ts() -> str:
    return (datetime.now(timezone.utc) - timedelta(seconds=PENDING_ACTION_TTL_SECONDS + 60)).isoformat()


# ── detect_intent ─────────────────────────────────────────────────────────────

def test_detect_cancel_order():
    assert detect_intent("tôi muốn hủy đơn hàng")[0] == "CANCEL_ORDER"


def test_detect_product_search():
    assert detect_intent("tôi muốn mua giày chạy bộ")[0] == "PRODUCT_SEARCH"


def test_detect_knowledge_qa():
    assert detect_intent("chính sách đổi trả như thế nào?")[0] == "KNOWLEDGE_QA"


def test_detect_order_status():
    assert detect_intent("đơn hàng của tôi đâu rồi?")[0] == "ORDER_STATUS"


def test_detect_size_advisor():
    assert detect_intent("tôi cao 1m70 nặng 65kg mặc size nào?")[0] == "SIZE_ADVISOR"


def test_detect_unknown():
    assert detect_intent("xin chào")[0] == "UNKNOWN"


def test_detect_ok_alone_is_unknown():
    # "ok" alone without pending → normal routing → UNKNOWN
    assert detect_intent("ok")[0] == "UNKNOWN"


def test_detect_add_to_cart():
    assert detect_intent("thêm vào giỏ hàng")[0] == "ADD_TO_CART"


def test_detect_product_detail():
    assert detect_intent("mẫu thứ 2 còn size không?")[0] == "PRODUCT_DETAIL"


def test_detect_sku_lookup_before_product_search():
    intent, confidence = detect_intent("SKU SEED-BOOT-RED-43 còn hàng không?")
    assert intent == "SKU_LOOKUP"
    assert confidence == 0.99


# ── CONFIRM_ACTION detection ──────────────────────────────────────────────────

async def test_confirm_signal_with_pending():
    state = _state("đồng ý", pending="cancel_order", created_at=_fresh_ts())
    result = await intent_router_node(state)
    assert result["intent"] == "CONFIRM_ACTION"


async def test_ok_signal_with_pending():
    state = _state("ok", pending="cancel_order", created_at=_fresh_ts())
    result = await intent_router_node(state)
    assert result["intent"] == "CONFIRM_ACTION"


async def test_oke_signal_with_pending():
    state = _state("oke", pending="add_to_cart", created_at=_fresh_ts())
    result = await intent_router_node(state)
    assert result["intent"] == "CONFIRM_ACTION"


async def test_confirm_with_trailing_words():
    # "đồng ý nha" is in signals set
    state = _state("đồng ý nha", pending="cancel_order", created_at=_fresh_ts())
    result = await intent_router_node(state)
    assert result["intent"] == "CONFIRM_ACTION"


# ── REJECT_ACTION detection ───────────────────────────────────────────────────

async def test_reject_signal_khong():
    state = _state("không", pending="cancel_order", created_at=_fresh_ts())
    result = await intent_router_node(state)
    assert result["intent"] == "REJECT_ACTION"


async def test_reject_signal_thoi():
    state = _state("thôi", pending="add_to_cart", created_at=_fresh_ts())
    result = await intent_router_node(state)
    assert result["intent"] == "REJECT_ACTION"


async def test_reject_signal_no():
    state = _state("no", pending="cancel_order", created_at=_fresh_ts())
    result = await intent_router_node(state)
    assert result["intent"] == "REJECT_ACTION"


# ── EXPIRED_CONFIRMATION ───────────────────────────────────────────────────────

async def test_expired_pending_confirm_returns_expired():
    state = _state("đồng ý", pending="cancel_order", created_at=_expired_ts())
    result = await intent_router_node(state)
    assert result["intent"] == "EXPIRED_CONFIRMATION"


async def test_expired_pending_reject_returns_expired():
    state = _state("không", pending="add_to_cart", created_at=_expired_ts())
    result = await intent_router_node(state)
    assert result["intent"] == "EXPIRED_CONFIRMATION"


# ── No-pending fallthrough ─────────────────────────────────────────────────────

async def test_ok_without_pending_routes_normally():
    state = _state("ok")
    result = await intent_router_node(state)
    assert result["intent"] == "UNKNOWN"


async def test_normal_message_no_pending():
    state = _state("tôi muốn tìm giày chạy bộ")
    result = await intent_router_node(state)
    assert result["intent"] == "PRODUCT_SEARCH"


async def test_confirm_without_pending_routes_normally():
    # "xác nhận" might match ORDER_STATUS or UNKNOWN if no pending
    state = _state("xác nhận")
    result = await intent_router_node(state)
    # No pending → goes to detect_intent("xác nhận") → UNKNOWN
    assert result["intent"] == "UNKNOWN"


async def test_pending_none_in_session_routes_normally():
    state = _state("đồng ý")
    state["session_context"] = {"pending_action": None}
    result = await intent_router_node(state)
    # pending is None → falsy → skip pending block → detect_intent
    assert result["intent"] == "UNKNOWN"


# ── Expired pending where user did NOT respond with confirm/reject ─────────────

async def test_expired_pending_non_confirm_routes_normally():
    # User sends normal message despite having an expired pending action
    state = _state("tôi muốn xem giày", pending="cancel_order", created_at=_expired_ts())
    result = await intent_router_node(state)
    # Not a confirm/reject signal → falls through to normal detect_intent
    assert result["intent"] == "PRODUCT_SEARCH"
