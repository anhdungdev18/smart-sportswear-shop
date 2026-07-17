from __future__ import annotations

import asyncio

from app.retrieval.product.parser.query_parser import ParsedQuery, parse_query
from app.retrieval.product.filters.product_filter import ProductFilter
from app.retrieval.product.keyword.keyword_retriever import retrieve as keyword_retrieve
from app.retrieval.product.enrich.variant_enricher import enrich
from app.retrieval.product.guards.result_guard import apply_guards
from app.retrieval.product.formatter.product_formatter import format_results
from app.retrieval.product.fusion import rrf_fusion
from app.retrieval.product.rerank import heuristic_reranker
from app.retrieval.product.query_rewrite import synonym_rewriter
from app.retrieval.product.query_rewrite import llm_rewriter
from app.schemas.product import ProductSearchResult, AppliedFilters
from app.observability.trace_logger import get_logger

logger = get_logger(__name__)

_LIMIT = 5
_CANDIDATE_MULTIPLIER = 2   # fetch wider candidates for fusion, cut to limit at rerank


async def search(
    query: str,
    limit: int = _LIMIT,
    parsed_query: ParsedQuery | dict | None = None,
) -> ProductSearchResult:
    """
    Hybrid retrieval pipeline:
      1. Try original query
      2. If empty → try rule-based synonym rewrite (fast, no LLM)
      3. If still empty → try LLM catalog rewrite (reads catalog.md)
    All attempts: keyword → vector (fail-safe) → RRF fusion → heuristic rerank.
    """
    logger.info(f"product_search | query={query!r}")

    result = await _pipeline(query, limit, _coerce_parsed(query, parsed_query))

    # Step 2: rule-based synonym rewrite
    if result.total == 0:
        rewritten = synonym_rewriter.rewrite(query)
        if rewritten.strip().lower() != query.strip().lower():
            logger.info(f"product_search | synonym_retry rewritten={rewritten!r}")
            result = await _pipeline(rewritten, limit)
            if result.total > 0:
                logger.info(f"product_search | synonym_rewrite_succeeded total={result.total}")

    # Step 3: LLM catalog rewrite — only when both step 1 and 2 produced nothing
    if result.total == 0:
        rewritten_llm = await llm_rewriter.rewrite(query)
        if rewritten_llm.strip().lower() != query.strip().lower():
            logger.info(f"product_search | llm_retry rewritten={rewritten_llm!r}")
            result = await _pipeline(rewritten_llm, limit)
            if result.total > 0:
                logger.info(f"product_search | llm_rewrite_succeeded total={result.total}")

    return result


def _coerce_parsed(query: str, parsed: ParsedQuery | dict | None) -> ParsedQuery:
    if isinstance(parsed, ParsedQuery):
        return parsed
    if isinstance(parsed, dict) and parsed:
        return ParsedQuery(
            raw=query,
            normalized=parsed.get("normalized") or query.lower().strip(),
            keyword=parsed.get("keyword") if parsed.get("keyword") is not None else query.lower().strip(),
            product_type=parsed.get("product_type"),
            sport_type_hint=parsed.get("sport_type_hint"),
            gender=parsed.get("gender"),
            brand=parsed.get("brand"),
            color=parsed.get("color"),
            price_min=parsed.get("price_min"),
            price_max=parsed.get("price_max"),
            feature_hints=list(parsed.get("feature_hints") or []),
        )
    return parse_query(query)


async def _pipeline(
    query: str,
    limit: int,
    parsed: ParsedQuery | None = None,
) -> ProductSearchResult:
    """
    Core hybrid search pipeline for one query string.
    Keyword branch is mandatory; vector branch is optional (fail-safe).
    """
    parsed = parsed or parse_query(query)
    logger.info(
        f"product_search | parsed type={parsed.product_type} sport={parsed.sport_type_hint} "
        f"gender={parsed.gender} price=[{parsed.price_min},{parsed.price_max}]"
    )

    candidate_limit = limit * _CANDIDATE_MULTIPLIER
    f = ProductFilter.from_parsed(parsed, limit=candidate_limit)

    # ── Keyword + Vector branches in parallel ────────────────────────────────
    # Both hit the DB independently (keyword SQL vs embed+pgvector); run concurrently
    # so the turn waits max(keyword, vector) instead of their sum.
    async def _safe_keyword() -> list[dict]:
        try:
            return await keyword_retrieve(f)
        except Exception as exc:
            logger.error(f"product_search | keyword_error={exc}")
            return []

    keyword_rows, vector_rows = await asyncio.gather(
        _safe_keyword(),
        _try_vector_retrieve(query, f),   # already fail-safe (returns [])
    )
    logger.info(f"product_search | keyword_hits={len(keyword_rows)} vector_hits={len(vector_rows)}")

    # ── Fusion ───────────────────────────────────────────────────────────────
    if vector_rows:
        fused = rrf_fusion.fuse(keyword_rows, vector_rows)
    else:
        fused = keyword_rows  # keyword-only path (Phase 3 behaviour preserved)

    logger.info(f"product_search | fused_count={len(fused)}")

    # ── Enrich → Guards → Rerank ─────────────────────────────────────────────
    enriched = enrich(fused)
    guarded  = apply_guards(enriched, f)
    reranked = heuristic_reranker.rerank(guarded, parsed, limit)

    logger.info(f"product_search | after_rerank={len(reranked)}")

    return format_results(reranked, f)


async def _try_vector_retrieve(query: str, f: ProductFilter) -> list[dict]:
    """
    Attempt vector retrieval using OpenAI text-embedding-3-small.
    Returns [] when: API key is missing, embedding API failed,
    product_embeddings table missing, or any other exception.
    All failures are logged at WARNING level; no exception propagates.
    """
    try:
        from app.retrieval.product.vector.vector_retriever import retrieve as vector_retrieve

        return await vector_retrieve(query, f)
    except Exception as exc:
        logger.warning(f"product_search | vector_error={exc!r} fallback=keyword_only")
        return []


def _empty_result(f: ProductFilter) -> ProductSearchResult:
    return ProductSearchResult(
        items=[],
        total=0,
        appliedFilters=AppliedFilters(
            keyword=f.keyword,
            productType=f.product_type,
            sportType=f.sport_type_hint,
            gender=f.gender,
            priceMin=f.price_min,
            priceMax=f.price_max,
        ),
    )
