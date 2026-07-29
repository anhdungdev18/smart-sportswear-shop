from __future__ import annotations

from pathlib import Path

import psycopg2


ROOT = Path(__file__).resolve().parents[1]
AI_ENV = ROOT / "ai_forecasting_service" / ".env"
CHATBOT_ENV = ROOT / "chatbot-admin-service" / ".env"
SCHEMA = ROOT / "chatbot-admin-service" / "db" / "admin_agent_approvals.sql"


def load_env(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = value.strip()
    return values


def upsert_env(path: Path, updates: dict[str, str]) -> None:
    existing_lines = path.read_text(encoding="utf-8").splitlines()
    seen: set[str] = set()
    next_lines: list[str] = []
    for line in existing_lines:
        if "=" not in line or line.strip().startswith("#"):
            next_lines.append(line)
            continue
        key = line.split("=", 1)[0].strip()
        if key in updates:
            next_lines.append(f"{key}={updates[key]}")
            seen.add(key)
        else:
            next_lines.append(line)
    for key, value in updates.items():
        if key not in seen:
            next_lines.append(f"{key}={value}")
    path.write_text("\n".join(next_lines) + "\n", encoding="utf-8")


def apply_schema(db_env: dict[str, str]) -> None:
    sslmode = "require" if "sslmode=require" in db_env.get("DB_PARAMS", "") else None
    connection = psycopg2.connect(
        host=db_env["DB_HOST"],
        port=int(db_env.get("DB_PORT", "5432")),
        dbname=db_env.get("DB_NAME", "postgres"),
        user=db_env["DB_USERNAME"],
        password=db_env["DB_PASSWORD"],
        sslmode=sslmode,
    )
    try:
        with connection.cursor() as cursor:
            cursor.execute(SCHEMA.read_text(encoding="utf-8"))
            cursor.execute(
                """
                select
                    exists(select 1 from information_schema.tables where table_name = 'admin_agent_approvals'),
                    exists(select 1 from information_schema.tables where table_name = 'admin_agent_approval_audit')
                """
            )
            exists = cursor.fetchone()
        connection.commit()
    finally:
        connection.close()
    print(f"approval_schema_exists={exists}")


def main() -> None:
    db_env = load_env(AI_ENV)
    upsert_env(
        CHATBOT_ENV,
        {
            "APPROVAL_STORAGE_BACKEND": "postgres",
            "APPROVAL_DB_HOST": db_env["DB_HOST"],
            "APPROVAL_DB_PORT": db_env.get("DB_PORT", "5432"),
            "APPROVAL_DB_NAME": db_env.get("DB_NAME", "postgres"),
            "APPROVAL_DB_USERNAME": db_env["DB_USERNAME"],
            "APPROVAL_DB_PASSWORD": db_env["DB_PASSWORD"],
            "APPROVAL_DB_SSLMODE": "require" if "sslmode=require" in db_env.get("DB_PARAMS", "") else "",
        },
    )
    apply_schema(db_env)
    print("chatbot_approval_backend=postgres")


if __name__ == "__main__":
    main()
