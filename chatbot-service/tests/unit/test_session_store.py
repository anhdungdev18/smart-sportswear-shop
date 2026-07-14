"""Unit tests for memory/base_store.py and memory/in_memory_store.py"""
from __future__ import annotations

import pytest
from datetime import datetime, timezone, timedelta

from app.memory.base_store import default_context, is_pending_expired, PENDING_ACTION_TTL_SECONDS
from app.memory.in_memory_store import InMemorySessionStore
from app.memory.session_store import now_iso


# ── default_context ───────────────────────────────────────────────────────────

def test_default_context_has_all_fields():
    ctx = default_context()
    expected_keys = {
        "last_intent", "last_product_ids", "last_products_summary",
        "selected_product_id", "selected_product_name", "selected_variant_hints",
        "pending_action", "pending_action_payload", "pending_action_created_at",
    }
    assert expected_keys.issubset(ctx.keys())


def test_default_context_pending_is_none():
    ctx = default_context()
    assert ctx["pending_action"] is None
    assert ctx["pending_action_payload"] == {}
    assert ctx["pending_action_created_at"] is None


def test_default_context_product_ids_empty():
    ctx = default_context()
    assert ctx["last_product_ids"] == []
    assert ctx["last_products_summary"] == []


# ── is_pending_expired ────────────────────────────────────────────────────────

def test_none_is_expired():
    assert is_pending_expired(None) is True


def test_old_timestamp_is_expired():
    old = datetime(2020, 1, 1, tzinfo=timezone.utc).isoformat()
    assert is_pending_expired(old) is True


def test_fresh_timestamp_not_expired():
    fresh = datetime.now(timezone.utc).isoformat()
    assert is_pending_expired(fresh) is False


def test_just_within_ttl():
    within = (datetime.now(timezone.utc) - timedelta(seconds=PENDING_ACTION_TTL_SECONDS - 10)).isoformat()
    assert is_pending_expired(within) is False


def test_just_past_ttl():
    past = (datetime.now(timezone.utc) - timedelta(seconds=PENDING_ACTION_TTL_SECONDS + 10)).isoformat()
    assert is_pending_expired(past) is True


def test_malformed_timestamp_is_expired():
    assert is_pending_expired("not-a-date") is True
    assert is_pending_expired("") is True


def test_naive_timestamp_treated_as_utc():
    # Naive ISO timestamp (no tz info) should be treated as UTC and checked
    fresh = datetime.now().isoformat()  # naive, no tzinfo
    assert is_pending_expired(fresh) is False


# ── InMemorySessionStore ──────────────────────────────────────────────────────

async def test_get_new_session_returns_default():
    store = InMemorySessionStore()
    ctx = await store.get("new_sess")
    expected = default_context()
    assert ctx == expected


async def test_save_and_get_round_trip():
    store = InMemorySessionStore()
    ctx = default_context()
    ctx["last_intent"] = "PRODUCT_SEARCH"
    ctx["selected_product_id"] = "prod_123"
    await store.save("sess1", ctx)

    loaded = await store.get("sess1")
    assert loaded["last_intent"] == "PRODUCT_SEARCH"
    assert loaded["selected_product_id"] == "prod_123"


async def test_get_returns_copy_not_reference():
    store = InMemorySessionStore()
    ctx1 = await store.get("sess1")
    ctx1["last_intent"] = "MUTATED"
    ctx2 = await store.get("sess1")
    assert ctx2["last_intent"] == ""  # internal state not affected


async def test_update_patches_fields():
    store = InMemorySessionStore()
    await store.update("sess1", last_intent="CANCEL_ORDER", selected_product_id="xyz")
    ctx = await store.get("sess1")
    assert ctx["last_intent"] == "CANCEL_ORDER"
    assert ctx["selected_product_id"] == "xyz"
    assert ctx["last_product_ids"] == []  # other fields unchanged


async def test_clear_pending_removes_pending_fields():
    store = InMemorySessionStore()
    await store.update("sess1",
        pending_action="cancel_order",
        pending_action_payload={"tool": "cancel_order"},
        pending_action_created_at=now_iso(),
        last_intent="CANCEL_ORDER",
    )
    await store.clear_pending("sess1")
    ctx = await store.get("sess1")
    assert ctx["pending_action"] is None
    assert ctx["pending_action_payload"] == {}
    assert ctx["pending_action_created_at"] is None
    assert ctx["last_intent"] == "CANCEL_ORDER"  # non-pending field preserved


async def test_multiple_sessions_isolated():
    store = InMemorySessionStore()
    await store.update("sess_A", last_intent="A_INTENT")
    await store.update("sess_B", last_intent="B_INTENT")
    assert (await store.get("sess_A"))["last_intent"] == "A_INTENT"
    assert (await store.get("sess_B"))["last_intent"] == "B_INTENT"


# ── now_iso ───────────────────────────────────────────────────────────────────

def test_now_iso_parseable():
    ts = now_iso()
    parsed = datetime.fromisoformat(ts)
    assert parsed.tzinfo is not None  # must be timezone-aware


def test_now_iso_recent():
    ts = now_iso()
    assert is_pending_expired(ts) is False
