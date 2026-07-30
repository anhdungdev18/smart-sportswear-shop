-- Visual product search persistence and reliable catalog-event outbox.
-- The vector extension is already required by product_embeddings, but keeping
-- this idempotent makes the dependency explicit for fresh environments.
create extension if not exists vector;

create schema if not exists visual_search;

create table visual_search.model_versions (
    id uuid primary key,
    provider varchar(50) not null,
    model varchar(150) not null,
    dimensions integer not null,
    status varchar(20) not null default 'BUILDING',
    target_image_count integer not null default 0,
    ready_image_count integer not null default 0,
    failed_image_count integer not null default 0,
    activated_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint uq_visual_model_provider_model unique (provider, model),
    constraint chk_visual_model_dimensions check (dimensions > 0),
    constraint chk_visual_model_status check (status in ('BUILDING', 'ACTIVE', 'INACTIVE', 'FAILED')),
    constraint chk_visual_model_coverage check (
        target_image_count >= 0 and ready_image_count >= 0 and failed_image_count >= 0
    )
);

create unique index uq_visual_model_single_active
    on visual_search.model_versions ((status))
    where status = 'ACTIVE';

create table visual_search.indexing_jobs (
    id uuid primary key,
    job_type varchar(30) not null,
    status varchar(20) not null default 'PENDING',
    model_version_id uuid references visual_search.model_versions (id),
    requested_by uuid references users (id) on delete set null,
    total_count integer not null default 0,
    pending_count integer not null default 0,
    processing_count integer not null default 0,
    completed_count integer not null default 0,
    failed_count integer not null default 0,
    source_counts jsonb not null default '{}'::jsonb,
    error_summary jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now(),
    started_at timestamptz,
    completed_at timestamptz,
    updated_at timestamptz not null default now(),
    constraint chk_visual_job_type check (job_type in ('IMAGE', 'PRODUCT', 'BACKFILL', 'RECONCILIATION')),
    constraint chk_visual_job_status check (status in ('PENDING', 'RUNNING', 'COMPLETED', 'PARTIAL', 'FAILED', 'CANCELLED')),
    constraint chk_visual_job_counts check (
        total_count >= 0 and pending_count >= 0 and processing_count >= 0
        and completed_count >= 0 and failed_count >= 0
    )
);

create table visual_search.image_embeddings (
    image_id uuid not null references product_images (id) on delete cascade,
    product_id uuid not null references products (id) on delete cascade,
    model_version_id uuid not null references visual_search.model_versions (id) on delete cascade,
    embedding vector(1024),
    image_hash varchar(64),
    source_etag varchar(255),
    source_version varchar(255),
    status varchar(20) not null default 'PENDING',
    attempts integer not null default 0,
    last_error text,
    last_attempt_at timestamptz,
    ready_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    primary key (image_id, model_version_id),
    constraint chk_visual_embedding_status check (status in ('PENDING', 'PROCESSING', 'READY', 'FAILED', 'STALE')),
    constraint chk_visual_embedding_attempts check (attempts >= 0),
    constraint chk_visual_embedding_hash check (image_hash is null or image_hash ~ '^[0-9a-f]{64}$'),
    constraint chk_visual_embedding_ready check (status <> 'READY' or (embedding is not null and image_hash is not null))
);

create index idx_visual_embeddings_product_model
    on visual_search.image_embeddings (product_id, model_version_id);
create index idx_visual_embeddings_model_status
    on visual_search.image_embeddings (model_version_id, status);

create table visual_search.processed_events (
    event_id uuid primary key,
    event_type varchar(80) not null,
    event_version integer not null,
    processed_at timestamptz not null default now(),
    constraint chk_visual_processed_event_version check (event_version > 0)
);

create table visual_search.usage_events (
    id uuid primary key,
    provider varchar(50) not null,
    model varchar(150) not null,
    operation varchar(30) not null,
    request_count integer not null default 1,
    text_tokens bigint not null default 0,
    image_pixels bigint not null default 0,
    estimated_cost_usd numeric(14, 8) not null default 0,
    latency_ms integer not null,
    success boolean not null,
    error_code varchar(100),
    occurred_at timestamptz not null default now(),
    constraint chk_visual_usage_operation check (operation in ('DOCUMENT_EMBEDDING', 'QUERY_EMBEDDING')),
    constraint chk_visual_usage_values check (
        request_count > 0 and text_tokens >= 0 and image_pixels >= 0
        and estimated_cost_usd >= 0 and latency_ms >= 0
    )
);

create index idx_visual_usage_occurred_at on visual_search.usage_events (occurred_at);

create table integration_outbox (
    id uuid primary key,
    event_type varchar(80) not null,
    event_version integer not null default 1,
    aggregate_type varchar(80) not null,
    aggregate_id uuid not null,
    payload jsonb not null,
    status varchar(20) not null default 'PENDING',
    attempts integer not null default 0,
    available_at timestamptz not null default now(),
    created_at timestamptz not null default now(),
    published_at timestamptz,
    last_error text,
    constraint chk_outbox_event_version check (event_version > 0),
    constraint chk_outbox_status check (status in ('PENDING', 'PUBLISHING', 'PUBLISHED', 'FAILED')),
    constraint chk_outbox_attempts check (attempts >= 0)
);

create index idx_outbox_publishable
    on integration_outbox (available_at, created_at)
    where status in ('PENDING', 'FAILED');
create index idx_outbox_aggregate on integration_outbox (aggregate_type, aggregate_id);
