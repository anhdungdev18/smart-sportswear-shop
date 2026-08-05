from __future__ import annotations

from app.retrieval.product.parser.query_parser import ParsedQuery
from app.retrieval.product.parser.query_parser import normalize_text
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)


def rerank(
    items: list[dict],
    parsed: ParsedQuery,
    limit: int,
) -> list[dict]:
    """
    Heuristic rerank on top of RRF-fused candidates.

    Signal weights (additive on top of RRF score):
    - Exact sport_type match           +0.30
    - Keyword token overlap in name    +0.10 per matching token (len > 2)
    - Vector score bonus (if present)  +0.20 * vector_score
    - Low stock penalty (<3 units)     -0.10
    - Out-of-stock penalty             -0.50

    Designed to be swapped for a cross-encoder model in a later phase.
    """
    normalized_query = normalize_text(parsed.raw)
    query_tokens = [t for t in normalize_text(parsed.keyword or "").split() if len(t) > 2]

    def _score(item: dict) -> float:
        s = item.get("_rrf_score", 0.0)

        name_normalized = normalize_text(item.get("name") or "")
        skus = [normalize_text(value) for value in item.get("available_skus") or []]
        if normalized_query == name_normalized or normalized_query in skus:
            s += 2.0

        # Sport-type exact match
        if parsed.sport_type_hint and item.get("sport_type"):
            if parsed.sport_type_hint.lower() in item["sport_type"].lower():
                s += 0.30

        # Keyword token overlap in product name
        if query_tokens and item.get("name"):
            overlaps = sum(1 for t in query_tokens if t in name_normalized)
            s += 0.10 * overlaps
            s += 0.20 * overlaps / len(query_tokens)

        # Vector score bonus (cosine similarity, 0-1 range)
        vec_score = item.get("vector_score")
        if vec_score is not None:
            s += 0.20 * float(vec_score)

        # Stock penalties
        avail = item.get("total_available", 0)
        if avail == 0:
            s -= 0.50
        elif avail < 3:
            s -= 0.10

        return s

    ranked = sorted(items, key=_score, reverse=True)
    top = ranked[:limit]

    logger.debug(f"heuristic_reranker | input={len(items)} output={len(top)} limit={limit}")
    return top
