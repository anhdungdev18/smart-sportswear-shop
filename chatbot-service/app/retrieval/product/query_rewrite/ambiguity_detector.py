from __future__ import annotations

import re

from app.retrieval.product.parser.query_parser import ParsedQuery


_NEED_CONTEXT_PATTERNS = (
    re.compile(r"\b(?:áo|quần|giày|đồ|trang phục)\s+(?:để\s+)?(?:đi|mặc|dùng)\b", re.IGNORECASE),
    re.compile(r"\b(?:đi|du lịch|đi chơi|đi phượt)\s+[\wÀ-ỹ]+", re.IGNORECASE),
    re.compile(r"\b(?:mặc|dùng)\s+(?:khi|lúc|ở|cho)\b", re.IGNORECASE),
    re.compile(r"\bphù hợp\s+(?:với|cho|để)\b", re.IGNORECASE),
    re.compile(
        r"\b(?:trời|thời tiết|mùa)\s+(?:lạnh|nóng|mưa|nắng|ẩm|rét)\b",
        re.IGNORECASE,
    ),
    re.compile(r"\b(?:ngoài trời|trong nhà|đường dài|du lịch|dã ngoại)\b", re.IGNORECASE),
)


def needs_pre_retrieval_rewrite(query: str, parsed: ParsedQuery) -> bool:
    """Detect need/occasion queries that lack searchable catalog attributes."""
    if parsed.sport_type_hint or parsed.feature_hints:
        return False
    normalized = " ".join(query.lower().split())
    return any(pattern.search(normalized) for pattern in _NEED_CONTEXT_PATTERNS)
