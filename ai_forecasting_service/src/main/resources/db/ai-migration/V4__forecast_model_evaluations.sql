create table if not exists forecast_model_evaluations (
    variant_id uuid primary key references ai_product_variant_snapshot (variant_id) on delete cascade,
    best_algorithm varchar(30) not null,
    mae numeric(12, 4),
    wape numeric(12, 6),
    residual_std_dev numeric(12, 4),
    confidence varchar(20) not null,
    last_evaluated_at timestamptz not null,
    fallback_reason text,
    algorithm_version integer not null default 1,
    constraint chk_model_evaluation_algorithm check (best_algorithm in ('MOVING_AVERAGE', 'EWMA', 'CROSTON')),
    constraint chk_model_evaluation_confidence check (confidence in ('LOW', 'MEDIUM', 'HIGH'))
);
