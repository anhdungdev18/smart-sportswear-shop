alter table ai_product_variant_snapshot
    add column if not exists data_source varchar(20) not null default 'REAL';

alter table ai_inventory_snapshot
    add column if not exists data_source varchar(20) not null default 'REAL';

alter table ai_sales_daily_snapshot
    add column if not exists data_source varchar(20) not null default 'REAL';

alter table ai_product_variant_snapshot
    drop constraint if exists ai_product_variant_snapshot_data_source_check;
alter table ai_inventory_snapshot
    drop constraint if exists ai_inventory_snapshot_data_source_check;
alter table ai_sales_daily_snapshot
    drop constraint if exists ai_sales_daily_snapshot_data_source_check;

alter table ai_product_variant_snapshot
    add constraint ai_product_variant_snapshot_data_source_check
    check (data_source in ('DEMO', 'REAL', 'IMPORTED'));
alter table ai_inventory_snapshot
    add constraint ai_inventory_snapshot_data_source_check
    check (data_source in ('DEMO', 'REAL', 'IMPORTED'));
alter table ai_sales_daily_snapshot
    add constraint ai_sales_daily_snapshot_data_source_check
    check (data_source in ('DEMO', 'REAL', 'IMPORTED'));

alter table ai_sales_daily_snapshot
    drop constraint if exists ai_sales_daily_snapshot_pkey;
alter table ai_sales_daily_snapshot
    add primary key (variant_id, sales_date, data_source);

create index if not exists idx_ai_product_variant_snapshot_source
    on ai_product_variant_snapshot(data_source);
create index if not exists idx_ai_inventory_snapshot_source_date
    on ai_inventory_snapshot(data_source, captured_at);
create index if not exists idx_ai_sales_daily_snapshot_source_date
    on ai_sales_daily_snapshot(data_source, sales_date);
