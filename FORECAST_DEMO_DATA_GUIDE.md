# Forecast demo data guide

This guide covers the Week 1 replenishment foundation: enabling the controlled
demo seed, locating its data, validating it, and sharing the implementation
with the team.

## Safety boundary

- Forecast demo orders are synthetic data, marked with `[FORECAST_DEMO]`.
- The forecast seed is disabled by default.
- Run it only against a disposable local/demo database.
- Never enable `APP_SEED_ENABLED` or `APP_FORECAST_DEMO_ENABLED` on production.
- Flyway V13 creates empty replenishment tables when deployed; it does not seed
  orders by itself.

## Where the data is stored

With the repository's Docker Compose setup, PostgreSQL stores data in the
Docker named volume `postgres_data`. It is not committed to Git and is not
automatically synchronized with any cloud database.

Source data:

- `product_variants`: 30 controlled SKUs with the `FD-` prefix.
- `orders`: about 3,000 synthetic orders marked `[FORECAST_DEMO]`.
- `order_items`: quantity sold for each variant/order.

Replenishment schema created by Flyway V13:

- `inventory_policies`
- `forecast_runs`
- `replenishment_recommendations`

At the end of Week 1, the last three tables are expected to be empty. Week 2
forecasting and recommendation services will populate them.

## Generate the local demo data

First start the local infrastructure from the repository root:

```powershell
docker compose up -d postgres redis
```

Copy `backend/.env.example` to `backend/.env`, then set:

```dotenv
APP_SEED_ENABLED=true
APP_FORECAST_DEMO_ENABLED=true
```

Start the backend using the team's existing IDE/local workflow. The core seed
runs first, followed by the forecast demo runner. Re-running the backend
replaces only orders whose note is exactly `[FORECAST_DEMO]`; it does not
delete user-created orders. Do not commit `backend/.env`.

## Open PostgreSQL

```powershell
docker compose exec postgres psql -U postgres -d dunghaiquyen
```

Useful checks:

```sql
-- Flyway schema version should be 13.
select installed_rank, version, description, success
from flyway_schema_history
order by installed_rank desc
limit 3;

-- Exactly 30 forecast variants.
select sku, color as demand_group, stock_quantity, reserved_quantity
from product_variants
where sku like 'FD-%'
order by sku;

-- Demo order count, date range, and status distribution.
select count(*) as orders, min(created_at), max(created_at)
from orders
where note = '[FORECAST_DEMO]';

select order_status, count(*)
from orders
where note = '[FORECAST_DEMO]'
group by order_status
order by order_status;

-- Daily valid demand by SKU. Missing dates are filled with zero by
-- DailyDemandService, not physically stored as zero rows in the database.
select
    oi.sku_snapshot,
    (o.created_at at time zone 'Asia/Ho_Chi_Minh')::date as demand_date,
    sum(oi.quantity) as quantity
from order_items oi
join orders o on o.id = oi.order_id
where o.note = '[FORECAST_DEMO]'
  and o.order_status in ('CONFIRMED', 'PACKING', 'SHIPPING', 'DELIVERED')
group by oi.sku_snapshot, demand_date
order by oi.sku_snapshot, demand_date;

-- Week 2 output tables (normally empty at the end of Week 1).
select count(*) from inventory_policies;
select count(*) from forecast_runs;
select count(*) from replenishment_recommendations;
```

Exit `psql` with `\q`.

You can also use DBeaver or pgAdmin with:

```text
Host: localhost
Port: 5434
Database: dunghaiquyen
Username: postgres
Password: postgres
```

## Local data versus cloud

The local Docker volume and a cloud PostgreSQL instance are independent.

- Pushing Git sends source code and migrations, never the local database rows.
- Deploying the backend to cloud runs V13 against the database configured by
  the cloud environment.
- Cloud demo data is created only if both seed flags are explicitly enabled.
- Keep both flags `false` in production.

For a shared demonstration, prefer one separately named demo database or let
each teammate generate the same data locally with random seed `2026`. Do not
copy synthetic orders into the production database.

## Team handoff checklist

Before opening a pull request:

```powershell
git status --short
git diff --check
```

Create a feature branch if the work is not already on one:

```powershell
git switch -c feat/replenishment-week1
```

This worktree already contained unrelated edits before replenishment work.
Review and stage deliberately. Use `git add -p` for files that contain mixed
changes, especially `application.yml`, `.env.example`, and `docker-compose.yml`.

Suggested commit split:

```text
feat(db): add replenishment forecasting schema
feat(seed): add reproducible forecast demo data
feat(report): aggregate daily demand by variant
fix(report): evict cached reports after inventory and order writes
fix(admin): remove fake forecast and silent inventory fallback
test(replenishment): cover demo seed and daily demand query
docs(replenishment): add local data and team handoff guide
```

Then push the branch and open a pull request:

```powershell
git push -u origin feat/replenishment-week1
```

The pull request should state:

- Data is synthetic and reproducible with seed `2026`.
- Demo seed is disabled by default and must stay disabled in production.
- Flyway V13 adds three tables.
- Backend: 391 tests pass.
- Admin frontend: lint and production build pass.
- Week 2 starts with the Moving Average baseline.
