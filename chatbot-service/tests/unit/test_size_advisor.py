"""Unit tests for services/size_advisor_service.py (pure functions only)"""
from __future__ import annotations

import pytest

from app.services.size_advisor_service import (
    _parse_height_cm,
    _parse_weight_kg,
    _suggest_size_from_body,
)


# ── _parse_height_cm ──────────────────────────────────────────────────────────

def test_height_1m70():
    assert _parse_height_cm("tôi cao 1m70") == 170


def test_height_1m7():
    # 1m7 → 1*100 + 7*10 = 170
    assert _parse_height_cm("cao 1m7") == 170


def test_height_1m65():
    assert _parse_height_cm("cao 1m65 nặng 60kg") == 165


def test_height_1m80():
    assert _parse_height_cm("1m80") == 180


def test_height_cm_form():
    assert _parse_height_cm("170cm") == 170


def test_height_cm_form_165():
    assert _parse_height_cm("cao 165cm") == 165


def test_height_none_when_missing():
    assert _parse_height_cm("tôi nặng 65kg") is None


def test_height_none_on_empty():
    assert _parse_height_cm("") is None


# ── _parse_weight_kg ──────────────────────────────────────────────────────────

def test_weight_65kg():
    assert _parse_weight_kg("65kg") == 65


def test_weight_in_sentence():
    assert _parse_weight_kg("tôi cao 1m70 nặng 72kg") == 72


def test_weight_100kg():
    assert _parse_weight_kg("100kg") == 100


def test_weight_none_when_missing():
    assert _parse_weight_kg("tôi cao 1m70") is None


def test_weight_none_on_empty():
    assert _parse_weight_kg("") is None


# ── _suggest_size_from_body ───────────────────────────────────────────────────
# Reference table:
#   XS: h 0–150,  w  0–48
#   S:  h 145–158, w 44–55
#   M:  h 155–165, w 50–63
#   L:  h 162–172, w 60–73
#   XL: h 170–180, w 70–83
#   XXL: h 177–999, w 78–999

def test_suggest_xs():
    # h=120, w=30: fits XS (0-150, 0-48) but NOT S (145-158, 44-55) → XS uniquely
    assert _suggest_size_from_body(120, 30) == "XS"


def test_suggest_s():
    # Clearly in S only, no overlap
    assert _suggest_size_from_body(150, 50) == "S"


def test_suggest_m():
    # h=160, w=58: fits M (155-165, 50-63)
    result = _suggest_size_from_body(160, 58)
    assert result == "M"


def test_suggest_l():
    # h=167, w=65: fits L (162-172, 60-73) only
    assert _suggest_size_from_body(167, 65) == "L"


def test_suggest_xl():
    # h=175, w=75: fits XL (170-180, 70-83)
    result = _suggest_size_from_body(175, 75)
    assert result == "XL"


def test_suggest_xxl():
    # h=185, w=90: fits XXL (177-999, 78-999)
    assert _suggest_size_from_body(185, 90) == "XXL"


def test_overlap_region_last_match_wins():
    # h=170, w=72: fits both L (162-172, 60-73) and XL (170-180, 70-83)
    # Last match wins → XL
    result = _suggest_size_from_body(170, 72)
    assert result == "XL"


def test_no_match_returns_none():
    # height 200, weight 120 → no rule covers this
    # XXL covers weight 78-999 and height 177-999, so 200/120 → XXL
    # Let's use an impossible combo: h=120, w=100 → no rule matches (XS is 0-150 h but 0-48 w)
    result = _suggest_size_from_body(120, 100)
    assert result is None


def test_boundary_exact_lower():
    # S lower bound: h=145, w=44
    assert _suggest_size_from_body(145, 44) == "S"


def test_boundary_exact_upper():
    # M upper bound: h=165, w=63
    result = _suggest_size_from_body(165, 63)
    # Also in L overlap: h=165 is between 162-172, w=63 is between 60-73 → L wins (last match)
    assert result == "L"
