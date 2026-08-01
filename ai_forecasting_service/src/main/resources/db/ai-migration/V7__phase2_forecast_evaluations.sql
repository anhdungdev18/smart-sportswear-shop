alter table forecast_model_evaluations
    drop constraint if exists chk_model_evaluation_algorithm,
    drop constraint if exists forecast_model_evaluations_best_algorithm_check,
    drop constraint if exists chk_model_evaluation_confidence,
    drop constraint if exists forecast_model_evaluations_confidence_check;

alter table forecast_model_evaluations
    add column if not exists data_source varchar(20) not null default 'REAL',
    add column if not exists demand_pattern varchar(40),
    add column if not exists bias numeric(12, 6),
    add column if not exists backtest_windows integer not null default 0,
    add column if not exists test_window_days integer not null default 30,
    add column if not exists training_from date,
    add column if not exists training_to date,
    add column if not exists benchmark_algorithm varchar(30),
    add column if not exists benchmark_mae numeric(12, 4),
    add column if not exists benchmark_wape numeric(12, 6),
    add column if not exists selection_reason text;

alter table forecast_model_evaluations
    add constraint chk_model_evaluation_algorithm
        check (best_algorithm in ('NAIVE', 'MOVING_AVERAGE', 'EWMA', 'CROSTON', 'ROBUST_MEDIAN')),
    add constraint chk_model_evaluation_confidence
        check (confidence in ('INSUFFICIENT', 'LOW', 'MEDIUM', 'HIGH'));

alter table forecast_runs
    drop constraint if exists chk_forecast_runs_algorithm,
    drop constraint if exists forecast_runs_algorithm_check,
    drop constraint if exists chk_forecast_runs_confidence,
    drop constraint if exists forecast_runs_confidence_check;

alter table forecast_runs
    add constraint chk_forecast_runs_algorithm
        check (algorithm in ('NAIVE', 'MOVING_AVERAGE', 'EWMA', 'CROSTON', 'ROBUST_MEDIAN')),
    add constraint chk_forecast_runs_confidence
        check (confidence in ('INSUFFICIENT', 'LOW', 'MEDIUM', 'HIGH'));

create index if not exists idx_forecast_model_evaluations_data_source
    on forecast_model_evaluations (data_source, last_evaluated_at desc);
