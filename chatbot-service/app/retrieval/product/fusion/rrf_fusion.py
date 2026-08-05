from __future__ import annotations

from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_DEFAULT_K = 60   # standard RRF constant; lower k amplifies rank differences
_KEYWORD_WEIGHT = 1.0
_VECTOR_WEIGHT = 1.0


def fuse(
    keyword_rows: list[dict],
    vector_rows: list[dict],
    k: int = _DEFAULT_K,
    keyword_weight: float = _KEYWORD_WEIGHT,
    vector_weight: float = _VECTOR_WEIGHT,
) -> list[dict]:
    """
    Reciprocal Rank Fusion (RRF) of keyword and vector result lists.

    score(d) = sum_i( weight_i / (k + rank_i(d)) )

    - Merges by product_id, deduplicates.
    - Preserves the full row data from the first source that returned it.
    - Attaches _rrf_score to each row for downstream reranker.
    - Returns rows ordered by RRF score descending.
    """
    scores: dict[str, float] = {}
    rows_by_id: dict[str, dict] = {}

    for rank, row in enumerate(keyword_rows, start=1):
        pid = row["product_id"]
        scores[pid] = scores.get(pid, 0.0) + keyword_weight / (k + rank)
        if pid not in rows_by_id:
            rows_by_id[pid] = {**row, "_source": "keyword"}

    for rank, row in enumerate(vector_rows, start=1):
        pid = row["product_id"]
        scores[pid] = scores.get(pid, 0.0) + vector_weight / (k + rank)
        if pid not in rows_by_id:
            rows_by_id[pid] = {**row, "_source": "vector"}
        else:
            # Preserve semantic evidence when the keyword row was inserted
            # first; downstream reranking otherwise cannot use vector_score.
            rows_by_id[pid]["vector_score"] = row.get("vector_score")
            rows_by_id[pid]["_source"] = "both"

    ordered = sorted(scores.keys(), key=lambda pid: scores[pid], reverse=True)

    result: list[dict] = []
    for pid in ordered:
        row = rows_by_id[pid]
        row["_rrf_score"] = scores[pid]
        result.append(row)

    keyword_only = sum(1 for r in result if r.get("_source") == "keyword")
    vector_only  = sum(1 for r in result if r.get("_source") == "vector")
    both         = len(keyword_rows) - keyword_only  # in keyword_rows AND vector_rows

    logger.debug(
        f"rrf_fusion | total={len(result)} "
        f"kw_only={keyword_only} vec_only={vector_only} both≈{both}"
    )
    return result
