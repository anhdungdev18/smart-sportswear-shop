create table if not exists forecast_demo_scenarios (
    id uuid primary key,
    variant_id uuid not null references product_variants (id) on delete cascade,
    marker varchar(100) not null,
    scenario_version varchar(100) not null,
    random_seed bigint not null,
    anchor_date date not null,
    history_days integer not null,
    demand_profile varchar(30) not null,
    expected_total_units integer not null default 0,
    expected_valid_units integer not null default 0,
    supplier_name varchar(255),
    lead_time_days integer,
    minimum_order_quantity integer,
    pack_size integer,
    service_level numeric(4, 3),
    metadata_json jsonb not null default '{}'::jsonb,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_forecast_demo_scenarios_variant_marker_v26 unique (variant_id, marker),
    constraint chk_forecast_demo_scenarios_profile_v26 check (demand_profile in (
        'SMOOTH', 'NORMAL', 'SLOW', 'INTERMITTENT', 'ERRATIC', 'GROWING', 'DECLINING', 'NEW_ITEM', 'NO_DEMAND'
    )),
    constraint chk_forecast_demo_scenarios_history_v26 check (history_days > 0),
    constraint chk_forecast_demo_scenarios_total_units_v26 check (expected_total_units >= 0),
    constraint chk_forecast_demo_scenarios_valid_units_v26 check (expected_valid_units >= 0),
    constraint chk_forecast_demo_scenarios_lead_time_v26 check (lead_time_days is null or lead_time_days >= 0),
    constraint chk_forecast_demo_scenarios_moq_v26 check (minimum_order_quantity is null or minimum_order_quantity > 0),
    constraint chk_forecast_demo_scenarios_pack_v26 check (pack_size is null or pack_size > 0),
    constraint chk_forecast_demo_scenarios_service_v26 check (service_level is null or service_level > 0 and service_level < 1)
);

create index if not exists idx_forecast_demo_scenarios_marker on forecast_demo_scenarios (marker);
create index if not exists idx_forecast_demo_scenarios_profile on forecast_demo_scenarios (demand_profile, marker);
