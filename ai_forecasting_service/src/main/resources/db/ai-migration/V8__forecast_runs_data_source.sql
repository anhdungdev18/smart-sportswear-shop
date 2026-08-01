alter table forecast_runs
    add column if not exists data_source varchar(20) not null default 'REAL';

alter table forecast_runs
    drop constraint if exists forecast_runs_data_source_check;

alter table forecast_runs
    add constraint forecast_runs_data_source_check
    check (data_source in ('DEMO', 'REAL', 'IMPORTED'));

create index if not exists idx_forecast_runs_data_source_generated
    on forecast_runs (data_source, generated_at desc);

create index if not exists idx_forecast_runs_variant_source_generated
    on forecast_runs (variant_id, data_source, generated_at desc);
