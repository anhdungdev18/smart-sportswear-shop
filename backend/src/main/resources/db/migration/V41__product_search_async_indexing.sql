create table if not exists product_search_processed_events (
    event_id uuid primary key,
    event_type varchar(80) not null,
    event_version integer not null,
    processed_at timestamptz not null default now(),
    constraint chk_product_search_processed_event_version check (event_version > 0)
);

create index if not exists idx_product_search_processed_at
    on product_search_processed_events (processed_at);
