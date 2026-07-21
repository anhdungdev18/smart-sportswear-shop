-- AI-owned read model. No foreign keys reference Core database tables.
create table if not exists ai_product_variant_snapshot (
    variant_id uuid primary key,
    product_id uuid not null,
    sku varchar(100) not null,
    product_name varchar(255) not null,
    size varchar(50) not null,
    color varchar(50) not null,
    captured_at timestamptz not null,
    source_updated_at timestamptz
);
create unique index if not exists uq_ai_product_variant_snapshot_sku on ai_product_variant_snapshot (sku);

create table if not exists ai_inventory_snapshot (
    variant_id uuid not null,
    stock_quantity integer not null,
    reserved_quantity integer not null,
    available_quantity integer generated always as (stock_quantity - reserved_quantity) stored,
    captured_at timestamptz not null,
    primary key (variant_id, captured_at),
    constraint chk_ai_inventory_stock check (stock_quantity >= 0),
    constraint chk_ai_inventory_reserved check (reserved_quantity >= 0),
    constraint chk_ai_inventory_reserved_le_stock check (reserved_quantity <= stock_quantity)
);
create index if not exists idx_ai_inventory_snapshot_latest on ai_inventory_snapshot (variant_id, captured_at desc);

create table if not exists ai_sales_daily_snapshot (
    variant_id uuid not null,
    sales_date date not null,
    quantity bigint not null,
    captured_at timestamptz not null,
    primary key (variant_id, sales_date),
    constraint chk_ai_sales_daily_quantity check (quantity >= 0)
);
create index if not exists idx_ai_sales_daily_snapshot_date on ai_sales_daily_snapshot (sales_date, variant_id);

create table if not exists ai_supplier_snapshot (
    supplier_id uuid primary key,
    supplier_code varchar(100) not null unique,
    supplier_name varchar(255) not null,
    active boolean not null default true,
    default_lead_time_days integer,
    captured_at timestamptz not null,
    source_updated_at timestamptz,
    constraint chk_ai_supplier_lead_time check (default_lead_time_days is null or default_lead_time_days >= 0)
);

alter table inventory_policies drop constraint if exists inventory_policies_variant_id_fkey;
alter table forecast_runs drop constraint if exists forecast_runs_variant_id_fkey;
alter table replenishment_recommendations drop constraint if exists replenishment_recommendations_variant_id_fkey;
alter table replenishment_recommendations drop constraint if exists replenishment_recommendations_acted_by_fkey;
