from __future__ import annotations

import re
import unicodedata
from dataclasses import dataclass, field, replace


@dataclass
class ParsedQuery:
    raw: str
    normalized: str
    keyword: str
    semantic_text: str = ""
    product_type: str | None = None
    sport_type_hint: str | None = None
    gender: str | None = None
    brand: str | None = None
    category: str | None = None
    color: str | None = None
    color_family: str | None = None
    surface: str | None = None
    size: str | None = None
    price_min: float | None = None
    price_max: float | None = None
    feature_hints: list[str] = field(default_factory=list)


GENDERS = ((("unisex", "cho ca hai"), "UNISEX"), (("nu", "women", "womens", "female"), "WOMEN"), (("nam", "men", "mens", "male"), "MEN"))
PRODUCT_TYPES = (
    (("giay", "dep", "sneaker", "boots"), "FOOTWEAR"),
    (("ao", "quan", "do mac", "trang phuc", "jersey"), "APPAREL"),
    (("phu kien", "balo", "tui", "mu", "tat", "vo"), "ACCESSORY"),
    (("thiet bi", "dung cu"), "EQUIPMENT"),
)
SPORTS = (
    (("bong da", "da banh", "football", "soccer"), "bóng đá"),
    (("chay bo", "running"), "chạy bộ"),
    (("bong ro", "basketball"), "bóng rổ"),
    (("boi loi", "swimming"), "bơi lội"),
    (("yoga",), "yoga"),
    (("leo nui", "hiking"), "leo núi"),
    (("dap xe", "cycling"), "đạp xe"),
    (("futsal",), "bóng đá"),
    (("gym", "tap ta"), "gym"),
    (("cau long", "badminton"), "cầu lông"),
    (("tennis",), "tennis"),
)
SURFACES = (
    (("co nhan tao", "san co nhan tao", "turf", "tf"), "TF"),
    (("co that", "san co that", "co tu nhien", "fg"), "FG"),
    (("ag",), "AG"),
    (("futsal", "san trong nha", "ic"), "IC"),
)
COLORS = (
    (("xanh duong", "xanh bien", "blue", "navy"), "BLUE"),
    (("xanh la", "green"), "GREEN"),
    (("den", "black"), "BLACK"),
    (("trang", "white"), "WHITE"),
    (("mau do", "red"), "RED"),
    (("hong", "pink"), "PINK"),
    (("vang", "yellow"), "YELLOW"),
    (("xam", "gray", "grey"), "GRAY"),
    (("nau", "brown"), "BROWN"),
    (("cam", "orange"), "ORANGE"),
    (("tim", "purple", "violet"), "PURPLE"),
    (("beige", "mau be"), "BEIGE"),
)
FEATURES = (
    (("thoang khi", "breathable"), "thoáng khí"),
    (("nhe", "lightweight"), "nhẹ"),
    (("toc do", "nhanh"), "tốc độ"),
    (("chong nuoc", "waterproof"), "chống nước"),
    (("troi nong", "mua he"), "trời nóng"),
    (("tien dao",), "tiền đạo"),
)

SIZE_RE = re.compile(r"\b(?:size|co)\s*[:\-]?\s*(?P<size>xxl|xl|xs|s|m|l|\d+(?:\.\d)?)\b", re.I)
SKU_SIZE_RE = re.compile(
    r"(?:(?:-|_)EU(?P<eu>\d{2}(?:\.\d)?)|(?:-|_)(?P<alpha>XXL|XL|XS|S|M|L)(?:-|_|$))",
    re.I,
)
RANGE_RE = re.compile(
    r"\btu\s+(?P<lo>\d+(?:[.,]\d+)?\s*(?:tr\d+|tr|k|trieu|nghin)?)\s+(?:den|toi|-)\s+"
    r"(?P<hi>\d+(?:[.,]\d+)?\s*(?:tr\d+|tr|k|trieu|nghin)?)\b"
)
MAX_RE = re.compile(r"\b(?:duoi|khong qua|toi da|max)\s+(?P<value>\d+(?:[.,]\d+)?\s*(?:tr\d+|tr|k|trieu|nghin)?)\b")
MIN_RE = re.compile(r"\b(?:tren|toi thieu|min)\s+(?P<value>\d+(?:[.,]\d+)?\s*(?:tr\d+|tr|k|trieu|nghin)?)\b")


def normalize_text(value: str) -> str:
    value = unicodedata.normalize("NFD", value.casefold())
    value = "".join(ch for ch in value if unicodedata.category(ch) != "Mn")
    return " ".join(value.replace("đ", "d").split())


def normalize_display(value: str) -> str:
    return " ".join(value.casefold().split())


def _first(text: str, mapping: tuple) -> tuple[str | None, str | None]:
    padded = f" {text} "
    for aliases, value in mapping:
        for alias in aliases:
            if re.search(rf"(?<!\w){re.escape(alias)}(?!\w)", padded):
                return value, alias
    return None, None


def _money(value: str, borrowed_unit: str | None = None) -> float:
    value = value.lower().replace(",", ".").replace(" ", "")
    unit = borrowed_unit
    number = value
    match = re.fullmatch(r"(?P<whole>\d+)tr(?P<fraction>\d+)", value)
    if match:
        return float(f"{match.group('whole')}.{match.group('fraction')}") * 1_000_000
    for suffix in ("trieu", "nghin", "tr", "k"):
        if value.endswith(suffix):
            unit, number = suffix, value[: -len(suffix)]
            break
    amount = float(number)
    if unit in ("trieu", "tr"):
        return amount * 1_000_000
    if unit in ("k", "nghin"):
        return amount * 1_000
    return amount


def parse_query(
    query: str,
    *,
    brands: list[str] | None = None,
    categories: list[str] | None = None,
    explicit_filters: dict | None = None,
) -> ParsedQuery:
    normalized = normalize_display(query)
    match_text = normalize_text(query)
    product_type, product_term = _first(match_text, PRODUCT_TYPES)
    if re.search(r"(?<!\w)đồ(?!\w)", normalized):
        product_type, product_term = "APPAREL", None
    sport, sport_term = _first(match_text, SPORTS)
    gender, gender_term = _first(match_text, GENDERS)
    surface, surface_term = _first(match_text, SURFACES)
    color_family, color_term = _first(match_text, COLORS)
    if re.search(r"(?<!\w)đỏ(?!\w)", normalized):
        color_family, color_term = "RED", None
    feature_hints = [value for aliases, value in FEATURES if any(alias in match_text for alias in aliases)]

    brand = next((value for value in brands or [] if normalize_text(value) in match_text), None)
    category = next(
        (
            value for value in sorted(categories or [], key=len, reverse=True)
            if len(normalize_text(value).split()) >= 3 and normalize_text(value) in match_text
        ),
        None,
    )
    size_match = SIZE_RE.search(match_text)
    size = size_match.group("size").upper() if size_match else None
    if size is None and ("-" in query or "_" in query):
        sku_size_match = SKU_SIZE_RE.search(query)
        if sku_size_match:
            size = (sku_size_match.group("eu") or sku_size_match.group("alpha")).upper()

    price_min = price_max = None
    consumed: list[str] = []
    range_match = RANGE_RE.search(match_text)
    if range_match:
        lo, hi = range_match.group("lo"), range_match.group("hi")
        unit = next((suffix for suffix in ("trieu", "tr", "k") if hi.endswith(suffix)), None)
        price_min, price_max = _money(lo, unit), _money(hi)
        consumed.append(range_match.group(0))
    else:
        max_match, min_match = MAX_RE.search(match_text), MIN_RE.search(match_text)
        if max_match:
            price_max = _money(max_match.group("value"))
            consumed.append(max_match.group(0))
        if min_match:
            price_min = _money(min_match.group("value"))
            consumed.append(min_match.group(0))

    semantic = match_text
    removable = consumed + [
        term for term in (gender_term, surface_term, color_term, size_match.group(0) if size_match else None) if term
    ]
    for term in removable:
        semantic = re.sub(rf"(?<!\w){re.escape(term)}(?!\w)", " ", semantic)
    semantic = " ".join(semantic.split()) or normalized

    result = ParsedQuery(
        raw=query,
        normalized=normalized,
        keyword=semantic,
        semantic_text=semantic,
        product_type=product_type,
        sport_type_hint=sport,
        gender=gender,
        brand=brand,
        category=category,
        color=color_family,
        color_family=color_family,
        surface=surface,
        size=size,
        price_min=price_min,
        price_max=price_max,
        feature_hints=feature_hints,
    )
    overrides = explicit_filters or {}
    field_map = {
        "gender": "gender", "sportType": "sport_type_hint", "productType": "product_type",
        "surface": "surface", "color": "color_family", "size": "size",
        "minPrice": "price_min", "maxPrice": "price_max",
    }
    for source, target in field_map.items():
        if overrides.get(source) is not None:
            result = replace(result, **{target: overrides[source]})
    return result
