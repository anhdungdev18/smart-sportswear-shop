ALTER TABLE forecast_runs ADD COLUMN IF NOT EXISTS daily_forecast jsonb;
