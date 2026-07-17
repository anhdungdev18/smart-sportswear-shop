from __future__ import annotations

import re

_EXPLICIT_SKU_RE = re.compile(
    r"\b(?:sku|mã\s+sku|mã\s+sản\s+phẩm|mã\s+hàng)\s*[:#-]?\s*"
    r"(?P<sku>[A-Za-z0-9][A-Za-z0-9._/-]{2,})\b",
    re.IGNORECASE,
)
_STANDALONE_SKU_RE = re.compile(
    r"\b(?=[A-Z0-9-]{4,}\b)(?=[A-Z0-9-]*[A-Z])(?=[A-Z0-9-]*\d)"
    r"[A-Z0-9]+(?:-[A-Z0-9]+)+\b"
)
_ORDER_CODE_RE = re.compile(r"^DH-?\d+$", re.IGNORECASE)


def extract_sku(message: str) -> str | None:
    """Extract an explicit or code-shaped SKU without confusing order codes."""
    explicit = _EXPLICIT_SKU_RE.search(message)
    candidate = explicit.group("sku") if explicit else None
    if candidate is None:
        standalone = _STANDALONE_SKU_RE.search(message)
        candidate = standalone.group(0) if standalone else None
    if not candidate or _ORDER_CODE_RE.fullmatch(candidate):
        return None
    return candidate.upper()
