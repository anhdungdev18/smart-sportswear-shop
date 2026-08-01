# Phase 0 — Voyage visual-search feasibility decision

Date: 2026-07-30

## Decision

**GO** for implementation with `voyage-multimodal-3.5` at 1024 dimensions, subject to the final out-of-catalog benchmark and production latency testing in the hardening phase.

The POC proved that both current catalog sources can pass through the same download, validation and normalization path and that Voyage query/document embeddings can retrieve alternate views of the same product. The result is a feasibility gate, not production-quality evidence: only four products were used because the new Voyage account is limited to 3 RPM and 10K TPM until billing is configured.

## Reproducible method

- Read the live catalog from Supabase using read-only queries.
- Select ACTIVE products with at least two images from each source.
- Use one catalog view as the document and a different view of the same product as its query ground truth.
- Require HTTPS and exact `res.cloudinary.com` or `cdn.shopify.com` hosts; reject non-public DNS targets.
- Enforce 5 MB download, 12 million decoded pixels, JPEG/PNG/WebP and no redirects.
- Normalize to RGB JPEG with a maximum 1024 × 1024 bounding box.
- Send normalized bytes as base64 to Voyage; use `input_type=document` and `input_type=query` separately.
- Rank by exact cosine similarity. No vectors, image bytes, credentials or connection details are saved.

Re-run from `visual-search-service/`:

```powershell
python scripts\phase0_voyage_poc.py --per-source 2
```

## Live catalog audit

| Measure | Result |
|---|---:|
| Products / ACTIVE / INACTIVE | 228 / 107 / 121 |
| Images / ACTIVE / INACTIVE | 676 / 576 / 100 |
| ACTIVE Shopify / Cloudinary images | 442 / 134 |
| ACTIVE products with / without images | 102 / 5 |
| Images missing `public_id` | 142 |
| Relative or non-HTTPS image paths | 100 |

These figures match the planning audit.

## POC result

| Measure | Result |
|---|---:|
| Valid product pairs | 4 (2 Cloudinary, 2 Shopify) |
| Categories represented | 2 (`giay-da-bong-fg`, `ao-chay-bo`) |
| Rejected candidates | 1 Shopify image over 12M decoded pixels |
| Embedding dimensions | 1024 |
| Recall@1 | 100% |
| Recall@5 | 100% |
| Category accuracy | 100% |
| Mean reciprocal rank | 1.0 |
| Document batch latency | 3623.88 ms |
| Query batch latency | 3653.32 ms |

Machine-readable evidence is in `phase0-poc-report.json`.

## Ground-truth policy for final acceptance

The final benchmark must contain at least 100 user-style query photographs not copied or cropped from catalog images. Each row must record query ID, expected product/category, gender, source/collection method and reviewer. Report Recall@1, Recall@5, category accuracy and p95 latency overall and by áo/quần/giày/phụ kiện. Phase 0's alternate-view set must not be reused as the acceptance dataset.

## Risks carried forward

- The POC sample is deliberately small due to the account's reduced Voyage limits.
- Batch latency exceeded the final 3-second request target; single-query p95 remains unmeasured.
- The catalog contains images over the 12M-pixel policy and 100 relative/non-HTTPS paths; backfill reporting must preserve these as source failures.
- Add a Voyage billing method or implement strict throttling before a larger benchmark/backfill.
