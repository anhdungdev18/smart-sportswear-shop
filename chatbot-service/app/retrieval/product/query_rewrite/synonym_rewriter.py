from __future__ import annotations

# Controlled synonym rewrite rules for Vietnamese sports queries.
# Rules must be specific enough to avoid false rewrites.
# Forbidden: adding brand / price / gender without user signal.
_RULES: list[tuple[str, str]] = [
    # Context-specific rewrites (most specific first)
    ("áo đi đà lạt",             "áo khoác giữ ấm"),
    ("đi đà lạt",                "leo núi giữ ấm"),
    ("giày đá banh sân cỏ nhân tạo", "giày bóng đá turf"),
    ("giày đá bóng sân cỏ nhân tạo", "giày bóng đá turf"),
    ("sân cỏ nhân tạo",          "turf bóng đá"),
    ("giày đá banh",             "giày bóng đá"),
    ("giày đá bóng",             "giày bóng đá"),
    # Equipment colloquialisms
    ("banh bóng đá",             "bóng bóng đá"),
    ("giày chạy",                "giày chạy bộ"),
    ("giày chạy marathon",       "giày chạy bộ marathon"),
    # Apparel shortenings
    ("đồ tập nữ",                "quần áo tập gym nữ"),
    ("đồ tập nam",               "quần áo tập gym nam"),
    ("đồ tập gym",               "quần áo tập gym"),
    ("đồ tập",                   "quần áo tập"),
    ("đồ chạy bộ",               "quần áo chạy bộ"),
    ("đồ chạy",                  "quần áo chạy bộ"),
    ("đồ bơi",                   "quần áo bơi lội"),
    ("đồ yoga",                  "quần áo yoga"),
    ("outfit tập",               "bộ quần áo tập"),
    ("set đồ tập",               "bộ quần áo tập"),
    ("bộ đồ tập",                "bộ quần áo tập"),
    # Footwear
    ("giày thể thao",            "giày thể thao"),  # identity — ensures correct tokenization
    ("dép thể thao",             "dép thể thao"),
]


def rewrite(query: str) -> str:
    """
    Apply controlled synonym rewrite to a normalized query.
    Returns the rewritten query, or the original if no rule matched.
    Never adds brand, price, or gender without an explicit signal.
    """
    lower = query.strip().lower()
    for pattern, replacement in _RULES:
        if pattern in lower:
            return lower.replace(pattern, replacement, 1)
    return query
