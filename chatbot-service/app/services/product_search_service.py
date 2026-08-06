from __future__ import annotations

import asyncio
import dataclasses
import hashlib
import json
import time
from collections import OrderedDict

from app.retrieval.product.parser.query_parser import ParsedQuery, parse_query, normalize_text
from app.retrieval.product.filters.product_filter import ProductFilter
from app.retrieval.product.keyword.keyword_retriever import retrieve as keyword_retrieve
from app.retrieval.product.enrich.variant_enricher import enrich
from app.retrieval.product.guards.result_guard import apply_guards
from app.retrieval.product.formatter.product_formatter import format_results
from app.retrieval.product.fusion import rrf_fusion
from app.retrieval.product.rerank import heuristic_reranker
from app.retrieval.product.query_rewrite import synonym_rewriter
from app.retrieval.product.query_rewrite import llm_rewriter
from app.retrieval.product.query_rewrite.semantic_expander import expand as expand_semantic_query
from app.retrieval.product.query_rewrite.ambiguity_detector import needs_pre_retrieval_rewrite
from app.schemas.product import ProductSearchResult, AppliedFilters
from app.observability.trace_logger import get_logger
from app.config.settings import settings
from app.repositories.product_repository import get_search_dictionaries

logger = get_logger(__name__)

_LIMIT = 5
_CANDIDATE_MULTIPLIER = 2   # fetch wider candidates for fusion, cut to limit at rerank
_candidate_cache: OrderedDict[str, tuple[float, dict]] = OrderedDict()
_CANDIDATE_MEMORY_MAX = 256


async def search_internal(request) -> dict:
    started = time.perf_counter()
    cache_key = _candidate_cache_key(request)
    cached = await _candidate_cache_get(cache_key)
    if cached is not None:
        cached["processingTimeMs"] = round((time.perf_counter() - started) * 1000)
        cached["cacheHit"] = True
        return cached
    brands, categories = await get_search_dictionaries()
    parsed = parse_query(
        request.query,
        brands=brands,
        categories=categories,
        explicit_filters=request.filters.model_dump(),
    )
    candidate_limit = min(400, max(request.limit * 4, (request.page * request.limit) * 2))
    expanded_semantic = expand_semantic_query(parsed.semantic_text or request.query)
    ranking_parsed = dataclasses.replace(parsed, keyword=expanded_semantic)
    product_filter = ProductFilter.from_parsed(ranking_parsed, limit=candidate_limit)

    async def safe_keyword() -> list[dict]:
        try:
            return await keyword_retrieve(product_filter)
        except Exception:
            logger.warning("product_search | keyword_branch_failed")
            return []

    keyword_rows, semantic_rows = await asyncio.gather(
        safe_keyword(),
        _try_vector_retrieve(
            expanded_semantic,
            product_filter,
        )
        if settings.PRODUCT_SEARCH_SEMANTIC_ENABLED else asyncio.sleep(0, result=[]),
    )
    minimum = settings.PRODUCT_SEARCH_MIN_SIMILARITY
    semantic_rows = [row for row in semantic_rows if float(row.get("vector_score", 0)) >= minimum]
    # Do not let embeddings turn an ungrounded, out-of-domain sentence into
    # arbitrary catalog matches. Structured constraints and recognized product
    # needs remain eligible for semantic-only retrieval.
    if (
        not ProductFilter.from_parsed(parsed).has_any_structural_filter()
        and not product_filter.feature_hints
        and not _is_catalog_grounded(parsed, keyword_rows)
    ):
        keyword_rows = []
        semantic_rows = []
    keyword_ranks = {row["product_id"]: rank for rank, row in enumerate(keyword_rows, 1)}
    semantic_ranks = {row["product_id"]: rank for rank, row in enumerate(semantic_rows, 1)}
    semantic_scores = {row["product_id"]: float(row.get("vector_score", 0)) for row in semantic_rows}
    fused = rrf_fusion.fuse(
        keyword_rows,
        semantic_rows,
        k=settings.PRODUCT_SEARCH_RRF_K,
        keyword_weight=settings.PRODUCT_SEARCH_KEYWORD_WEIGHT,
        vector_weight=settings.PRODUCT_SEARCH_SEMANTIC_WEIGHT,
    )
    ranked = heuristic_reranker.rerank(fused, ranking_parsed, len(fused))
    start, end = (request.page - 1) * request.limit, request.page * request.limit
    items = []
    for row in ranked[start:end]:
        product_id = row["product_id"]
        reasons = []
        if product_id in keyword_ranks:
            reasons.append("KEYWORD")
        if product_id in semantic_ranks:
            reasons.append("SEMANTIC")
        if parsed.brand:
            reasons.append("BRAND")
        if parsed.category:
            reasons.append("CATEGORY")
        items.append({
            "productId": product_id,
            "keywordRank": keyword_ranks.get(product_id),
            "semanticRank": semantic_ranks.get(product_id),
            "semanticScore": semantic_scores.get(product_id),
            "fusionScore": float(row.get("_rrf_score", 0)),
            "matchedReasons": reasons,
        })
    response = {
        "items": items,
        "total": len(ranked),
        "parsedQuery": {
            "normalized": normalize_text(parsed.normalized),
            "semanticText": parsed.semantic_text,
            "category": parsed.category,
            "brand": parsed.brand,
            "gender": parsed.gender,
            "sportType": parsed.sport_type_hint,
            "productType": parsed.product_type,
            "surface": parsed.surface,
            "colorFamily": parsed.color_family,
            "size": parsed.size,
            "minPrice": parsed.price_min,
            "maxPrice": parsed.price_max,
            "featureHints": parsed.feature_hints,
        },
        "searchMode": "HYBRID" if semantic_rows else "KEYWORD",
        "processingTimeMs": round((time.perf_counter() - started) * 1000),
        "cacheHit": False,
    }
    await _candidate_cache_set(cache_key, response)
    return response


def _candidate_cache_key(request) -> str:
    payload = json.dumps(
        {
            "request": request.model_dump(),
            "semanticEnabled": settings.PRODUCT_SEARCH_SEMANTIC_ENABLED,
            "minSimilarity": settings.PRODUCT_SEARCH_MIN_SIMILARITY,
            "rrfK": settings.PRODUCT_SEARCH_RRF_K,
            "keywordWeight": settings.PRODUCT_SEARCH_KEYWORD_WEIGHT,
            "semanticWeight": settings.PRODUCT_SEARCH_SEMANTIC_WEIGHT,
        },
        ensure_ascii=False,
        sort_keys=True,
        separators=(",", ":"),
    )
    digest = hashlib.sha256(payload.encode("utf-8")).hexdigest()
    return f"search:hybrid:v1:{digest}"


async def _candidate_cache_get(key: str) -> dict | None:
    now = time.monotonic()
    memory = _candidate_cache.get(key)
    if memory and memory[0] > now:
        _candidate_cache.move_to_end(key)
        return json.loads(json.dumps(memory[1]))
    if memory:
        _candidate_cache.pop(key, None)
    if not settings.REDIS_URL:
        return None
    try:
        from redis.asyncio import Redis
        client = Redis.from_url(settings.REDIS_URL, socket_connect_timeout=.15, socket_timeout=.15)
        raw = await client.get(key)
        await client.aclose()
        if raw:
            value = json.loads(raw)
            _candidate_cache[key] = (now + settings.PRODUCT_SEARCH_CANDIDATE_CACHE_TTL_SECONDS, value)
            return value
    except Exception:
        pass
    return None


async def _candidate_cache_set(key: str, value: dict) -> None:
    ttl = settings.PRODUCT_SEARCH_CANDIDATE_CACHE_TTL_SECONDS
    _candidate_cache[key] = (time.monotonic() + ttl, json.loads(json.dumps(value)))
    _candidate_cache.move_to_end(key)
    while len(_candidate_cache) > _CANDIDATE_MEMORY_MAX:
        _candidate_cache.popitem(last=False)
    if not settings.REDIS_URL:
        return
    try:
        from redis.asyncio import Redis
        client = Redis.from_url(settings.REDIS_URL, socket_connect_timeout=.15, socket_timeout=.15)
        await client.setex(key, ttl, json.dumps(value, separators=(",", ":")))
        await client.aclose()
    except Exception:
        pass


def _is_catalog_grounded(parsed: ParsedQuery, rows: list[dict]) -> bool:
    ignored = {"sieu", "cap", "san", "pham", "hang", "shop"}
    tokens = {
        token for token in normalize_text(parsed.keyword or parsed.raw).split()
        if len(token) >= 3 and token not in ignored
    }
    if not tokens:
        return False
    for row in rows:
        catalog_text = normalize_text(" ".join([
            row.get("name") or "",
            row.get("brand_name") or "",
            row.get("category_name") or "",
        ]))
        if any(token in catalog_text for token in tokens):
            return True
    return False


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
    logger.info("product_search | query_received=true")

    parsed = _coerce_parsed(query, parsed_query)
    pre_rewrite_attempted = False

    # Need/occasion queries contain useful human context but poor catalog terms.
    # Rewrite them before retrieval instead of waiting for a predictable miss.
    if needs_pre_retrieval_rewrite(query, parsed):
        pre_rewrite_attempted = True
        rewritten_need = await llm_rewriter.rewrite_ambiguous_need(query)
        if rewritten_need.strip().casefold() != query.strip().casefold():
            logger.info(f"product_search | ambiguous_pre_rewrite rewritten={rewritten_need!r}")
            rewritten_result = await _pipeline(rewritten_need, limit)
            # Once an occasion query has been converted into a concrete catalog
            # need, falling back to the broad original query reintroduces the
            # exact false positives the rewrite is meant to prevent.
            return rewritten_result

    # The original query remains the source-of-truth fallback whenever LLM
    # inference fails or the inferred catalog query has no matching inventory.
    result = await _pipeline(query, limit, parsed)

    # Colour-drop retry: catalogue colours are often English/descriptive
    # ("Sân khách", "Red Color", "Icon"), so a Vietnamese colour filter such as
    # "đỏ" can zero out otherwise-valid results. Retry once without it.
    if result.total == 0 and parsed.color:
        import dataclasses
        result = await _pipeline(query, limit, dataclasses.replace(parsed, color=None))
        if result.total > 0:
            logger.info(f"product_search | colour_drop_retry_succeeded total={result.total}")

    # Step 2: rule-based synonym rewrite
    if result.total == 0:
        rewritten = synonym_rewriter.rewrite(query)
        if rewritten.strip().lower() != query.strip().lower():
            logger.info(f"product_search | synonym_retry rewritten={rewritten!r}")
            result = await _pipeline(rewritten, limit)
            if result.total > 0:
                logger.info(f"product_search | synonym_rewrite_succeeded total={result.total}")

    # Step 3: LLM catalog rewrite — only when both step 1 and 2 produced nothing
    if result.total == 0 and not pre_rewrite_attempted:
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
