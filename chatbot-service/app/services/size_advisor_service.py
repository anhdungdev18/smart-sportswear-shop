from __future__ import annotations

import re

from app.schemas.product import SizeAdviceResult
from app.memory import context_resolver
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_HEIGHT_RE = re.compile(r"(\d+)\s*m\s*(\d{1,2})\b", re.IGNORECASE)   # 1m70, 1m7
_HEIGHT_CM_RE = re.compile(r"\b(\d{2,3})\s*cm\b", re.IGNORECASE)      # 170cm
_WEIGHT_RE = re.compile(r"\b(\d{2,3})\s*kg\b", re.IGNORECASE)         # 65kg

# Vietnamese unisex clothing reference (h_min cm, h_max cm, w_min kg, w_max kg, size)
_CLOTHING_RULES: list[tuple[int, int, int, int, str]] = [
    (0,   150, 0,  48, "XS"),
    (145, 158, 44, 55, "S"),
    (155, 165, 50, 63, "M"),
    (162, 172, 60, 73, "L"),
    (170, 180, 70, 83, "XL"),
    (177, 999, 78, 999, "XXL"),
]

_SIZE_CAVEAT = (
    "Đây là gợi ý tham khảo dựa trên chiều cao và cân nặng — "
    "kích thước thực tế có thể khác nhau tùy thương hiệu và kiểu dáng. "
    "Bạn nên thử trực tiếp hoặc tham khảo bảng size cụ thể của sản phẩm."
)


def _parse_height_cm(text: str) -> int | None:
    m = _HEIGHT_RE.search(text)
    if m:
        h = int(m.group(1)) * 100
        tail = m.group(2)
        h += int(tail) if len(tail) == 2 else int(tail) * 10
        return h
    m = _HEIGHT_CM_RE.search(text)
    return int(m.group(1)) if m else None


def _parse_weight_kg(text: str) -> int | None:
    m = _WEIGHT_RE.search(text)
    return int(m.group(1)) if m else None


def _suggest_size_from_body(height_cm: int, weight_kg: int) -> str | None:
    best: str | None = None
    for h_min, h_max, w_min, w_max, size in _CLOTHING_RULES:
        if h_min <= height_cm <= h_max and w_min <= weight_kg <= w_max:
            best = size  # last match wins for overlapping ranges
    return best


async def advise(
    message: str,
    product_id: str | None = None,
) -> SizeAdviceResult:
    """
    Size advice in priority order:
    1. Specific product in context → real available sizes from DB
    2. Height + weight both present → rule-based clothing estimate with caveat
    3. General question → knowledge retrieval from corpus
    """
    # Priority 1: product in context
    if product_id:
        from app.services.product_detail_service import get_detail

        detail = await get_detail(product_id)
        if detail.found:
            avail_sizes = sorted({
                v.size for v in detail.variants
                if v.size and v.available > 0
            })
            logger.info(
                f"size_advisor | source=product_variants product_id={product_id} "
                f"sizes={avail_sizes}"
            )
            return SizeAdviceResult(
                source="product_variants",
                productName=detail.name,
                availableSizes=avail_sizes,
            )

    # Priority 2: body measurements
    height = _parse_height_cm(message)
    weight = _parse_weight_kg(message)

    if height is not None and weight is not None:
        suggested = _suggest_size_from_body(height, weight)
        logger.info(
            f"size_advisor | source=rule_based height={height} weight={weight} "
            f"suggested={suggested}"
        )
        return SizeAdviceResult(
            source="rule_based",
            suggestedSize=suggested,
            caveat=_SIZE_CAVEAT,
        )

    # Priority 3: knowledge retrieval
    from app.services.knowledge_search_service import search as knowledge_search

    kr = knowledge_search(query=message, limit=1)
    if kr.answers:
        best = kr.answers[0]
        logger.info(f"size_advisor | source=knowledge title={best.title!r}")
        return SizeAdviceResult(
            source="knowledge",
            knowledgeTitle=best.title,
            knowledgeContent=best.content,
        )

    logger.info(f"size_advisor | source=unknown query={message!r}")
    return SizeAdviceResult(
        source="unknown",
        error=(
            "Tôi chưa tìm được hướng dẫn size phù hợp. "
            "Bạn có thể liên hệ shop để được tư vấn trực tiếp."
        ),
    )
