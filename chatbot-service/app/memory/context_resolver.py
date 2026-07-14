from __future__ import annotations

import re

# Ordinal references: phrase → 0-based index into last_product_ids
_ORDINAL_MAP: list[tuple[str, int]] = [
    ("mẫu đầu tiên", 0), ("mẫu thứ nhất", 0), ("cái đầu tiên", 0), ("cái thứ nhất", 0),
    ("mẫu thứ 2", 1), ("mẫu thứ hai", 1), ("cái thứ 2", 1), ("cái thứ hai", 1),
    ("mẫu thứ 3", 2), ("mẫu thứ ba", 2), ("cái thứ 3", 2), ("cái thứ ba", 2),
    ("mẫu thứ 4", 3), ("mẫu thứ tư", 3), ("cái thứ 4", 3), ("cái thứ tư", 3),
    ("mẫu thứ 5", 4), ("mẫu thứ năm", 4), ("cái thứ 5", 4), ("cái thứ năm", 4),
]

_THIS_REFS = frozenset({
    "mẫu này", "cái này", "sản phẩm này",
    "mẫu đó", "cái đó", "giày đó", "áo đó", "quần đó",
})

_SIZE_RE = re.compile(r"\bsize\s+([smlxXSML]{1,3}|\d{2,3})\b", re.IGNORECASE)

# Longer keys first to prevent "xanh" shadowing "xanh lá" / "xanh navy"
_COLOR_MAP: list[tuple[str, str]] = [
    ("xanh lá", "xanh lá"), ("xanh navy", "xanh navy"), ("xanh dương", "xanh dương"),
    ("xanh", "xanh"), ("đen", "đen"), ("trắng", "trắng"), ("đỏ", "đỏ"),
    ("vàng", "vàng"), ("cam", "cam"), ("hồng", "hồng"), ("tím", "tím"),
    ("xám", "xám"), ("nâu", "nâu"), ("bạc", "bạc"), ("kem", "kem"), ("be", "be"),
]


def resolve_product_reference(message: str, ctx: dict) -> str | None:
    """
    Resolve which product the user is referencing.

    Priority:
    1. Explicit ordinal ("mẫu thứ 2") → last_product_ids[1]
    2. Proximal reference ("mẫu này") → selected_product_id or last_product_ids[0]
    3. Name substring match in last_products_summary
    4. None when context is ambiguous

    Returns product_id string or None.
    """
    lower = message.lower()
    last_ids: list[str] = ctx.get("last_product_ids") or []
    summary: list[dict] = ctx.get("last_products_summary") or []
    selected = ctx.get("selected_product_id")

    # 1. Ordinal reference (longest match wins — list is ordered longest-first per group)
    for phrase, idx in _ORDINAL_MAP:
        if phrase in lower:
            if idx < len(last_ids):
                return last_ids[idx]
            return None  # ordinal out of range → don't fall through

    # 2. Proximal reference
    for ref in _THIS_REFS:
        if ref in lower:
            return selected or (last_ids[0] if last_ids else None)

    # 3. Name match (require at least 4 chars to avoid single-word false positives)
    for item in summary:
        name_lower = (item.get("name") or "").lower()
        if len(name_lower) >= 4 and name_lower in lower:
            return item.get("id")

    return None


def parse_variant_hints(message: str) -> dict[str, str]:
    """
    Extract size and/or color hints from natural language.
    Returns dict with keys "size" and/or "color".
    """
    hints: dict[str, str] = {}

    m = _SIZE_RE.search(message)
    if m:
        hints["size"] = m.group(1).upper()

    lower = message.lower()
    for color_key, color_val in _COLOR_MAP:
        if color_key in lower:
            hints["color"] = color_val
            break

    return hints
