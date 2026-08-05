-- Product hybrid search metadata. This migration is additive and rollback-safe:
-- existing display colors and embeddings are preserved.
create extension if not exists unaccent;
create extension if not exists pg_trgm;
create extension if not exists pgcrypto;

alter table product_embeddings
    add column if not exists embedding_model varchar(100),
    add column if not exists embedding_dimensions integer,
    add column if not exists content_hash char(64),
    add column if not exists status varchar(20) not null default 'READY',
    add column if not exists last_error text;

update product_embeddings
set embedding_model = coalesce(embedding_model, 'text-embedding-3-small'),
    embedding_dimensions = coalesce(embedding_dimensions, 1536),
    content_hash = coalesce(
        content_hash,
        encode(digest(coalesce(document_text, ''), 'sha256'), 'hex')
    )
where embedding_model is null
   or embedding_dimensions is null
   or content_hash is null;

alter table product_embeddings
    add constraint chk_product_embeddings_dimensions
        check (embedding_dimensions is null or embedding_dimensions > 0),
    add constraint chk_product_embeddings_status
        check (status in ('PENDING', 'READY', 'FAILED', 'STALE'));

alter table product_variants
    add column if not exists color_family varchar(20),
    add column if not exists edition varchar(100);

create index if not exists idx_product_variants_color_family
    on product_variants (color_family)
    where color_family is not null;

-- Exact search is sufficient for the current small vector catalog. These
-- indexes improve normalized keyword fallback without touching visual_search.
create index if not exists idx_products_name_trgm
    on products using gin (lower(name) gin_trgm_ops);
create index if not exists idx_product_variants_sku_trgm
    on product_variants using gin (lower(sku) gin_trgm_ops);
