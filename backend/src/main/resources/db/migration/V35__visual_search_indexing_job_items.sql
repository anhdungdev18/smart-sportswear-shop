-- Correlate catalog events with backfill/reconciliation jobs without adding
-- job metadata to the deliberately small catalog-event v1 contract.
create table visual_search.indexing_job_items (
    job_id uuid not null references visual_search.indexing_jobs (id) on delete cascade,
    image_id uuid not null references product_images (id) on delete cascade,
    event_id uuid not null unique references integration_outbox (id) on delete cascade,
    status varchar(20) not null default 'PENDING',
    last_error text,
    created_at timestamptz not null default now(),
    completed_at timestamptz,
    updated_at timestamptz not null default now(),
    primary key (job_id, image_id),
    constraint chk_visual_job_item_status
        check (status in ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

create index idx_visual_job_items_event on visual_search.indexing_job_items (event_id);
create index idx_visual_job_items_job_status on visual_search.indexing_job_items (job_id, status);
