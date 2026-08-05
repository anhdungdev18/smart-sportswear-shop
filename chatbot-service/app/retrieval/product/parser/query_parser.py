from __future__ import annotations

import re
from dataclasses import dataclass, field


@dataclass
class ParsedQuery:
    raw: str
    normalized: str
    keyword: str                        # full normalized query for text search
    product_type: str | None = None     # APPAREL | FOOTWEAR | ACCESSORY | EQUIPMENT
    sport_type_hint: str | None = None  # raw text, used for ILIKE on p.sport_type
    gender: str | None = None           # MEN | WOMEN | UNISEX
    brand: str | None = None            # exact brand name (Nike | Adidas | Puma | Under Armour)
    color: str | None = None            # ILIKE token on pv.color (e.g. "đỏ", "xanh dương")
    price_min: float | None = None
    price_max: float | None = None
    feature_hints: list[str] = field(default_factory=list)


# ── Mapping tables ──────────────────────────────────────────────────────────

_PRODUCT_TYPE_MAP: list[tuple[list[str], str]] = [
    (["giày", "dép", "sneaker", "boot"], "FOOTWEAR"),
    (["áo", "quần", "shirt", "jersey", "legging", "shorts", "trang phục"], "APPAREL"),
    (["phụ kiện", "balo", "túi", "mũ", "tất", "vớ", "băng tay", "băng đầu",
      "bình nước", "băng cổ tay"], "ACCESSORY"),
    (["thiết bị", "dụng cụ", "equipment", "bóng", "vợt", "cung"], "EQUIPMENT"),
]

_SPORT_TYPE_MAP: list[tuple[list[str], str]] = [
    (["bóng đá", "football", "soccer"], "bóng đá"),
    (["chạy bộ", "chạy", "running"], "chạy bộ"),
    (["gym", "tập gym", "tập tạ", "thể hình"], "gym"),
    (["cầu lông", "badminton"], "cầu lông"),
    (["tennis"], "tennis"),
    (["bóng rổ", "basketball"], "bóng rổ"),
    (["bơi", "bơi lội", "swimming"], "bơi lội"),
    (["yoga"], "yoga"),
    (["leo núi", "hiking"], "leo núi"),
    (["đạp xe", "cycling"], "đạp xe"),
]

_GENDER_MAP: list[tuple[list[str], str]] = [
    (["nam", "men", "male", "con trai"], "MEN"),
    (["nữ", "women", "female", "con gái"], "WOMEN"),
    (["unisex", "cho cả hai"], "UNISEX"),
]

# Brand — closed set; value matches the exact brands.name string in DB.
_BRAND_MAP: list[tuple[list[str], str]] = [
    (["nike"], "Nike"),
    (["adidas"], "Adidas"),
    (["puma"], "Puma"),
    (["under armour", "under armor", "underarmour"], "Under Armour"),
]

# Color — value is the ILIKE token matched against pv.color (stored as "Đỏ/Đen" etc.).
# Compound colors MUST precede the generic "xanh" so "xanh dương" wins over "xanh".
_COLOR_MAP: list[tuple[list[str], str]] = [
    (["xanh dương", "xanh biển", "xanh da trời"], "xanh dương"),
    (["xanh lá", "xanh lá cây", "xanh lục"], "xanh lá"),
    (["xanh navy", "navy"], "navy"),
    (["đỏ"], "đỏ"),
    (["cam"], "cam"),
    (["vàng"], "vàng"),
    (["tím"], "tím"),
    (["hồng"], "hồng"),
    (["xám", "ghi"], "xám"),
    (["đen"], "đen"),
    (["trắng"], "trắng"),
    (["xanh"], "xanh"),   # generic — matches both blue and green variants via ILIKE
]

_FEATURE_MAP: list[tuple[list[str], str]] = [
    (["nhẹ", "siêu nhẹ", "lightweight"], "nhẹ"),
    (["thoáng", "thoáng khí", "thông thoáng", "breathable"], "thoáng khí"),
    (["giữ ấm", "ấm", "chống lạnh"], "giữ ấm"),
    (["chống trượt", "không trượt"], "chống trượt"),
    (["bám sân", "grip"], "bám sân"),
    (["chống nước", "waterproof", "không thấm"], "chống nước"),
    (["co giãn", "đàn hồi", "stretch"], "co giãn"),
    (["nhanh khô", "dry-fit", "dryfit"], "nhanh khô"),
]

# Price parsing: match "X nghìn/ngàn", "X k", "X triệu/tr", "X.X triệu"
_PRICE_UNIT_RE = re.compile(
    r"(?P<val>[\d,.]+)\s*(?P<unit>triệu|tr\b|nghìn|ngàn|k\b)",
    re.IGNORECASE | re.UNICODE,
)

_PRICE_RANGE_RE = re.compile(
    # "từ 6 đến 8 triệu" — the low number may omit the unit and borrow it from the high one.
    r"(từ|khoảng)\s+(?P<lo>[\d,.]+\s*(?:triệu|tr\b|nghìn|ngàn|k\b)?)"
    r"\s*(đến|tới|-)\s+(?P<hi>[\d,.]+\s*(?:triệu|tr\b|nghìn|ngàn|k\b))",
    re.IGNORECASE | re.UNICODE,
)

_PRICE_MAX_RE = re.compile(
    r"(dưới|tối đa|không quá|max)\s+(?P<val>[\d,.]+\s*(?:triệu|tr\b|nghìn|ngàn|k\b))",
    re.IGNORECASE | re.UNICODE,
)

_PRICE_MIN_RE = re.compile(
    r"(trên|từ|tối thiểu|min)\s+(?P<val>[\d,.]+\s*(?:triệu|tr\b|nghìn|ngàn|k\b))",
    re.IGNORECASE | re.UNICODE,
)


def _parse_price_value(text: str) -> float:
    m = _PRICE_UNIT_RE.search(text)
    if not m:
        return 0.0
    raw_val = m.group("val").replace(",", ".").strip()
    val = float(raw_val)
    unit = m.group("unit").lower()
    if unit in ("triệu", "tr"):
        return val * 1_000_000
    if unit in ("nghìn", "ngàn", "k"):
        return val * 1_000
    return val


def _normalize(query: str) -> str:
    return " ".join(query.lower().split())


def _first_match(text: str, mapping: list[tuple[list[str], str]]) -> str | None:
    for keywords, value in mapping:
        for kw in keywords:
            if kw in text:
                return value
    return None


def _all_matches(text: str, mapping: list[tuple[list[str], str]]) -> list[str]:
    results = []
    for keywords, value in mapping:
        for kw in keywords:
            if kw in text:
                results.append(value)
                break
    return results


def _match_with_term(text: str, mapping: list[tuple[list[str], str]]) -> tuple[str | None, str | None]:
    """Like _first_match but also returns the surface term that matched (for stripping)."""
    for keywords, value in mapping:
        for kw in keywords:
            if kw in text:
                return value, kw
    return None, None


def _strip_terms(keyword: str, terms: list[str]) -> str:
    """Remove whole-word occurrences of the given terms (brand/color/"màu") from keyword."""
    for t in terms:
        if t:
            keyword = re.sub(rf"\b{re.escape(t)}\b", " ", keyword)
    return " ".join(keyword.split())


def parse_query(query: str) -> ParsedQuery:
    normalized = _normalize(query)

    product_type = _first_match(normalized, _PRODUCT_TYPE_MAP)
    sport_type_hint = _first_match(normalized, _SPORT_TYPE_MAP)
    gender = _first_match(normalized, _GENDER_MAP)
    brand, brand_term = _match_with_term(normalized, _BRAND_MAP)
    color, color_term = _match_with_term(normalized, _COLOR_MAP)
    feature_hints = _all_matches(normalized, _FEATURE_MAP)

    # Brand/color become hard filters → strip them (and "màu") from the keyword AND-split
    # so a non-name word like "đỏ"/"nike" can't zero out the keyword branch.
    # Category words ("áo"/"giày"/"đá bóng") are deliberately KEPT in the keyword: they
    # match the Vietnamese category names ("Áo Đá Bóng" vs "Giày Đá Bóng") and are what
    # discriminates a jersey query from a footwear query in this catalog.
    keyword = _strip_terms(normalized, [brand_term, color_term, "màu", "màu sắc"])

    price_min: float | None = None
    price_max: float | None = None
    price_spans: list[str] = []

    range_m = _PRICE_RANGE_RE.search(normalized)
    if range_m:
        lo_txt, hi_txt = range_m.group("lo"), range_m.group("hi")
        # "từ 6 đến 8 triệu": lo omits the unit → borrow it from hi.
        if not _PRICE_UNIT_RE.search(lo_txt):
            unit_m = _PRICE_UNIT_RE.search(hi_txt)
            if unit_m:
                lo_txt = f"{lo_txt.strip()} {unit_m.group('unit')}"
        price_min = _parse_price_value(lo_txt)
        price_max = _parse_price_value(hi_txt)
        price_spans.append(range_m.group(0))
    else:
        max_m = _PRICE_MAX_RE.search(normalized)
        if max_m:
            price_max = _parse_price_value(max_m.group("val"))
            price_spans.append(max_m.group(0))
        min_m = _PRICE_MIN_RE.search(normalized)
        if min_m:
            price_min = _parse_price_value(min_m.group("val"))
            price_spans.append(min_m.group(0))

    # Strip the matched price phrases from the keyword so filler words like
    # "dưới"/"triệu"/"đến" don't AND-fail the keyword branch — price is a numeric filter.
    if price_spans:
        keyword = _strip_terms(keyword, price_spans)

    return ParsedQuery(
        raw=query,
        normalized=normalized,
        keyword=keyword,   # may be "" when the query was only brand/color/price → keyword branch skipped
        product_type=product_type,
        sport_type_hint=sport_type_hint,
        gender=gender,
        brand=brand,
        color=color,
        price_min=price_min if price_min else None,
        price_max=price_max if price_max else None,
        feature_hints=feature_hints,
    )
