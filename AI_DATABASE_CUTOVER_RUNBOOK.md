# AI Database cutover checklist

## Required environment

Core:
- `AI_SYNC_SECRET`: shared service secret.
- Existing Core datasource variables remain unchanged.

AI:
- `DB_*`: points only to the AI database.
- `CORE_API_BASE_URL`: Core service URL, not a database URL.
- `AI_SYNC_SECRET`: exactly the same value as Core.
- `SPRING_FLYWAY_ENABLED=true` on the new AI database.

## Data copy and reconciliation

Run the AI service once against an empty AI database so Flyway creates V1-V2. Copy the three owned tables from Core in this order:

1. `inventory_policies`
2. `forecast_runs`
3. `replenishment_recommendations`

Do not move or delete the Core tables during this step. For every table compare:

```sql
select count(*) from inventory_policies;
select count(*) from forecast_runs;
select count(*) from replenishment_recommendations;
select status, count(*) from replenishment_recommendations group by status order by status;
```

Then call `POST /api/v1/admin/replenishment/snapshots/sync` with an Admin JWT. A successful response reports `capturedAt`, `variants`, `dailySalesRows`, and `suppliers`. Verify:

```sql
select count(*) from ai_product_variant_snapshot;
select count(distinct variant_id) from ai_inventory_snapshot;
select min(sales_date), max(sales_date), sum(quantity) from ai_sales_daily_snapshot;
select count(*) from ai_supplier_snapshot;
```

## Rollback

Point Admin back to Core and leave the copied AI database untouched for diagnosis. The legacy Core tables are the rollback source. Never drop them in the same release that performs cutover.

## Final cleanup

After the observation window and signed reconciliation, add a new Core Flyway migration that drops only the three legacy AI tables. Do not delete old migration files or manually edit `flyway_schema_history`.
