# Product hybrid search rollout and rollback

## Preconditions

- Flyway migrations through V41 applied.
- ACTIVE embedding coverage is at least 98% for the configured model/dimensions.
- Chatbot, backend, and storefront verification gates pass.
- The 60-query benchmark passes structured-filter, relevance, zero-result, and
  latency targets.
- `product-search.indexing` queue, retry queues, and DLQ exist and the indexing
  worker is healthy.

## Rollout

1. Deploy backend and chatbot-service with
   `PRODUCT_HYBRID_SEARCH_ENABLED=false` and
   `PRODUCT_SEARCH_INDEXING_ENABLED=false`.
2. Apply migrations and run the embedding coverage check.
3. Start the indexing worker, enable `PRODUCT_SEARCH_INDEXING_ENABLED`, and
   verify a reindex event reaches `product-search.indexing`.
4. Verify content-hash skip, retry, DLQ, and reconciliation metrics/logs.
5. Run the versioned benchmark and fallback smoke test.
6. Enable hybrid search for internal/test traffic first.
7. Enable storefront traffic only after all acceptance gates pass.
8. Monitor p95 latency, fallback rate, zero-result rate, queue depth, DLQ depth,
   embedding failures, and search click-through.

## Rollback

1. Set `PRODUCT_HYBRID_SEARCH_ENABLED=false`.
2. Keep the keyword endpoint available; do not couple cart or checkout to the
   indexing worker.
3. If the worker is unhealthy, set `PRODUCT_SEARCH_INDEXING_ENABLED=false`.
4. Do not delete migrations, embeddings, outbox rows, retry messages, or DLQ
   messages during the incident.
5. Preserve logs and benchmark evidence, fix forward, then repeat the rollout
   gates.
