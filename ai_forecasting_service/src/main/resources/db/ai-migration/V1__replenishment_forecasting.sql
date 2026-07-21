-- Inventory replenishment forecasting (MVP).

create table if not exists inventory_policies (
    id uuid primary key,
    variant_id uuid not null,
    lead_time_days integer not null default 7,
    target_cover_days integer not null default 30,
    service_level numeric(4, 3) not null default 0.950,
    minimum_order_quantity integer not null default 1,
    pack_size integer not null default 1,
    supplier_name varchar(255),
    active boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_inventory_policies_variant unique (variant_id),
    constraint chk_inventory_policies_lead_time check (lead_time_days >= 0),
    constraint chk_inventory_policies_target_cover check (target_cover_days > 0),
    constraint chk_inventory_policies_service_level check (service_level > 0 and service_level < 1),
    constraint chk_inventory_policies_minimum_order check (minimum_order_quantity > 0),
    constraint chk_inventory_policies_pack_size check (pack_size > 0)
);

create index if not exists idx_inventory_policies_active on inventory_policies (active);

create table if not exists forecast_runs (
    id uuid primary key,
    variant_id uuid not null,
    algorithm varchar(30) not null,
    training_from date not null,
    training_to date not null,
    forecast_horizon_days integer not null,
    average_daily_demand numeric(12, 4) not null,
    forecast_quantity numeric(12, 4) not null,
    mae numeric(12, 4),
    wape numeric(12, 6),
    residual_std_dev numeric(12, 4),
    confidence varchar(20) not null,
    generated_at timestamptz not null,
    constraint chk_forecast_runs_algorithm check (algorithm in ('MOVING_AVERAGE', 'EWMA', 'CROSTON')),
    constraint chk_forecast_runs_horizon check (forecast_horizon_days > 0),
    constraint chk_forecast_runs_confidence check (confidence in ('LOW', 'MEDIUM', 'HIGH'))
);

create index if not exists idx_forecast_runs_variant_generated
    on forecast_runs (variant_id, generated_at desc);

create table if not exists replenishment_recommendations (
    id uuid primary key,
    variant_id uuid not null,
    forecast_run_id uuid references forecast_runs (id) on delete set null,
    available_quantity integer not null,
    incoming_quantity integer not null default 0,
    reorder_point integer not null,
    safety_stock integer not null,
    suggested_quantity integer not null,
    admin_quantity integer,
    estimated_stockout_days integer,
    priority varchar(20) not null,
    status varchar(20) not null default 'PENDING',
    explanation_json jsonb not null,
    admin_note text,
    acted_by uuid,
    acted_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_replenishment_priority check (priority in ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    constraint chk_replenishment_status check (status in ('PENDING', 'ACCEPTED', 'ADJUSTED', 'DISMISSED', 'RECEIVED')),
    constraint chk_replenishment_available check (available_quantity >= 0),
    constraint chk_replenishment_incoming check (incoming_quantity >= 0),
    constraint chk_replenishment_reorder_point check (reorder_point >= 0),
    constraint chk_replenishment_safety_stock check (safety_stock >= 0),
    constraint chk_replenishment_suggested check (suggested_quantity >= 0),
    constraint chk_replenishment_admin_quantity check (admin_quantity is null or admin_quantity >= 0)
);

create index if not exists idx_replenishment_status_priority
    on replenishment_recommendations (status, priority, created_at desc);

create index if not exists idx_replenishment_variant_created
    on replenishment_recommendations (variant_id, created_at desc);

-- Generation updates the existing pending row. This also protects that rule
-- from concurrent requests at the database boundary.
create unique index if not exists uq_replenishment_pending_variant
    on replenishment_recommendations (variant_id)
    where status = 'PENDING';

