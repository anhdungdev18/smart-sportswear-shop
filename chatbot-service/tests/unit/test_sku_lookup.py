from __future__ import annotations

import pytest

from app.retrieval.product.parser.sku_parser import extract_sku
from app.services import sku_lookup_service


@pytest.mark.parametrize(
    "message,expected",
    [
        ("SKU SEED-BOOT-RED-43 còn hàng không?", "SEED-BOOT-RED-43"),
        ("mã sản phẩm abc-123-xl", "ABC-123-XL"),
        ("kiểm tra RUN-42-BLK", "RUN-42-BLK"),
        ("đơn hàng DH-123 đang ở đâu", None),
        ("tôi muốn mua giày chạy bộ", None),
    ],
)
def test_extract_sku(message, expected):
    assert extract_sku(message) == expected


def _row(sku: str) -> dict:
    return {
        "product_id": "product-1",
        "variant_id": "variant-1",
        "name": "Giày chạy bộ",
        "slug": "giay-chay-bo",
        "sku": sku,
        "category_name": "Giày chạy bộ",
        "brand_name": "Nike",
        "color": "Đen",
        "size": "42",
        "price": 900000,
        "available": 4,
        "variant_status": "ACTIVE",
        "primary_image": None,
    }


@pytest.mark.asyncio
async def test_exact_lookup_does_not_run_partial(monkeypatch):
    calls = []

    async def fake_lookup(sku, *, exact, limit):
        calls.append(exact)
        return [_row(sku)] if exact else []

    monkeypatch.setattr(sku_lookup_service.product_repository, "find_variants_by_sku", fake_lookup)
    result = await sku_lookup_service.lookup("seed-boot-red-43")

    assert calls == [True]
    assert result.matchType == "exact"
    assert result.items[0].sku == "SEED-BOOT-RED-43"


@pytest.mark.asyncio
async def test_partial_lookup_runs_only_after_exact_miss(monkeypatch):
    calls = []

    async def fake_lookup(sku, *, exact, limit):
        calls.append(exact)
        return [] if exact else [_row("SEED-BOOT-RED-42"), _row("SEED-BOOT-RED-43")]

    monkeypatch.setattr(sku_lookup_service.product_repository, "find_variants_by_sku", fake_lookup)
    result = await sku_lookup_service.lookup("boot-red")

    assert calls == [True, False]
    assert result.matchType == "partial"
    assert result.total == 2
