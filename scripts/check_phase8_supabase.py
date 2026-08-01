from __future__ import annotations

from pathlib import Path

import psycopg2


def load_env(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = value.strip()
    return values


def main() -> None:
    env = load_env(Path("ai_forecasting_service/.env"))
    sslmode = "require" if "sslmode=require" in env.get("DB_PARAMS", "") else None
    connection = psycopg2.connect(
        host=env["DB_HOST"],
        port=int(env.get("DB_PORT", "5432")),
        dbname=env.get("DB_NAME", "postgres"),
        user=env["DB_USERNAME"],
        password=env["DB_PASSWORD"],
        sslmode=sslmode,
    )
    try:
        with connection.cursor() as cursor:
            queries = [
                (
                    "replenishment_by_status",
                    "select status, count(*) from replenishment_recommendations group by status order by status",
                ),
                (
                    "pending_replenishment",
                    "select count(*), count(distinct variant_id) from replenishment_recommendations where status = 'PENDING'",
                ),
                (
                    "pending_demo_replenishment",
                    """
                    select count(*)
                    from replenishment_recommendations r
                    join ai_product_variant_snapshot v on v.variant_id = r.variant_id
                    where r.status = 'PENDING' and v.data_source = 'DEMO'
                    """,
                ),
                (
                    "ai_demo_snapshots",
                    """
                    select
                        (select count(*) from ai_product_variant_snapshot where data_source = 'DEMO') as demo_variants,
                        (select count(*) from ai_sales_daily_snapshot where data_source = 'DEMO') as demo_sales_rows,
                        (select count(*) from demand_classifications where data_source = 'DEMO') as demo_classifications,
                        (select count(*) from forecast_model_evaluations where data_source = 'DEMO') as demo_evaluations
                    """,
                ),
                (
                    "admin_approval_tables_exist",
                    """
                    select
                        exists(select 1 from information_schema.tables where table_name = 'admin_agent_approvals'),
                        exists(select 1 from information_schema.tables where table_name = 'admin_agent_approval_audit')
                    """,
                ),
                (
                    "admin_approval_counts",
                    """
                    select
                        case when exists(select 1 from information_schema.tables where table_name = 'admin_agent_approvals')
                             then (select count(*) from admin_agent_approvals)
                             else null end,
                        case when exists(select 1 from information_schema.tables where table_name = 'admin_agent_approval_audit')
                             then (select count(*) from admin_agent_approval_audit)
                             else null end
                    """,
                ),
            ]
            for name, sql in queries:
                cursor.execute(sql)
                print(f"{name}: {cursor.fetchall()}")
    finally:
        connection.close()


if __name__ == "__main__":
    main()
