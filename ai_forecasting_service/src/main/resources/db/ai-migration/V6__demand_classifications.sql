create table if not exists demand_classifications (
    id uuid primary key default gen_random_uuid(),
    variant_id uuid not null,
    sku varchar(120) not null,
    product_name varchar(255),
    data_source varchar(20) not null default 'REAL',
    from_date date not null,
    to_date date not null,
    history_days integer not null,
    non_zero_days integer not null,
    total_units bigint not null,
    adi numeric(12, 4) not null,
    cv_squared numeric(12, 4) not null,
    trend_slope numeric(12, 4) not null,
    classification varchar(40) not null,
    confidence varchar(20) not null,
    reason text not null,
    algorithm_version varchar(80) not null,
    classified_at timestamptz not null default now(),
    constraint demand_classifications_source_check check (data_source in ('DEMO', 'REAL', 'IMPORTED')),
    constraint demand_classifications_pattern_check check (
        classification in (
            'NO_DEMAND',
            'NEW_ITEM',
            'INTERMITTENT',
            'ERRATIC',
            'SMOOTH',
            'GROWING',
            'DECLINING',
            'INSUFFICIENT_DATA'
        )
    ),
    constraint demand_classifications_confidence_check check (confidence in ('HIGH', 'MEDIUM', 'LOW')),
    constraint demand_classifications_window_check check (from_date <= to_date),
    unique (variant_id, data_source, algorithm_version)
);

create index if not exists idx_demand_classifications_source_pattern
    on demand_classifications(data_source, classification);

create index if not exists idx_demand_classifications_variant_source
    on demand_classifications(variant_id, data_source);
