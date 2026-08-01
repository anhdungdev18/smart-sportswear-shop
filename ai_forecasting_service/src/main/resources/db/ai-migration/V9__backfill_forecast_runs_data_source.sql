update forecast_runs f
set data_source = e.data_source
from forecast_model_evaluations e
where e.variant_id = f.variant_id
  and e.data_source in ('DEMO', 'REAL', 'IMPORTED')
  and f.data_source is distinct from e.data_source;
