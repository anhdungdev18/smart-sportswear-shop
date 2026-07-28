from __future__ import annotations

import json
import sqlite3
import threading
from pathlib import Path
from typing import Any, Protocol

import psycopg2
import psycopg2.extras

from app.config.settings import settings

_LOCK = threading.RLock()


class ApprovalStoreProtocol(Protocol):
    def list(self, limit: int = 50, status_filter: str | None = None) -> list[dict[str, Any]]: ...
    def get(self, approval_id: str) -> dict[str, Any] | None: ...
    def get_by_idempotency_key(self, idempotency_key: str) -> dict[str, Any] | None: ...
    def save(self, row: dict[str, Any]) -> dict[str, Any]: ...
    def execution_result(self, idempotency_key: str) -> dict[str, Any] | None: ...
    def clear_for_tests(self) -> None: ...


class BaseApprovalStore:
    def execution_result(self, idempotency_key: str) -> dict[str, Any] | None:
        row = self.get_by_idempotency_key(idempotency_key)
        if row and row["status"] == "EXECUTED":
            return row
        return None

    def _payload_json(self, row: dict[str, Any]) -> str:
        return json.dumps(row, sort_keys=True, separators=(",", ":"), default=str)


class SQLiteApprovalStore(BaseApprovalStore):
    def __init__(self, sqlite_path: str | None = None) -> None:
        self.sqlite_path = Path(sqlite_path or settings.APPROVAL_SQLITE_PATH)
        self._initialized = False

    def list(self, limit: int = 50, status_filter: str | None = None) -> list[dict[str, Any]]:
        self._ensure_schema()
        with _LOCK, self._connect() as connection:
            if status_filter:
                rows = connection.execute(
                    "select payload_json from admin_agent_approvals where status = ? order by created_at desc limit ?",
                    (status_filter, limit),
                ).fetchall()
            else:
                rows = connection.execute(
                    "select payload_json from admin_agent_approvals order by created_at desc limit ?",
                    (limit,),
                ).fetchall()
        return [json.loads(row["payload_json"]) for row in rows]

    def get(self, approval_id: str) -> dict[str, Any] | None:
        self._ensure_schema()
        with _LOCK, self._connect() as connection:
            row = connection.execute(
                "select payload_json from admin_agent_approvals where id = ?",
                (approval_id,),
            ).fetchone()
        return json.loads(row["payload_json"]) if row else None

    def get_by_idempotency_key(self, idempotency_key: str) -> dict[str, Any] | None:
        self._ensure_schema()
        with _LOCK, self._connect() as connection:
            row = connection.execute(
                "select payload_json from admin_agent_approvals where idempotency_key = ?",
                (idempotency_key,),
            ).fetchone()
        return json.loads(row["payload_json"]) if row else None

    def save(self, row: dict[str, Any]) -> dict[str, Any]:
        self._ensure_schema()
        with _LOCK, self._connect() as connection:
            connection.execute(
                _SQLITE_UPSERT,
                _approval_values(row, self._payload_json(row)),
            )
            self._replace_audit(connection, row)
            connection.commit()
        return row

    def clear_for_tests(self) -> None:
        self._ensure_schema()
        with _LOCK, self._connect() as connection:
            connection.execute("delete from admin_agent_approval_audit")
            connection.execute("delete from admin_agent_approvals")
            connection.commit()

    def _replace_audit(self, connection: sqlite3.Connection, row: dict[str, Any]) -> None:
        connection.execute("delete from admin_agent_approval_audit where approval_id = ?", (row["id"],))
        for index, event in enumerate(row.get("audit", [])):
            connection.execute(_SQLITE_AUDIT_INSERT, _audit_values(row["id"], index, event))

    def _connect(self) -> sqlite3.Connection:
        connection = sqlite3.connect(self.sqlite_path)
        connection.row_factory = sqlite3.Row
        return connection

    def _ensure_schema(self) -> None:
        if self._initialized:
            return
        self.sqlite_path.parent.mkdir(parents=True, exist_ok=True)
        with _LOCK, self._connect() as connection:
            _create_schema(connection)
            connection.commit()
        self._initialized = True


ApprovalStore = SQLiteApprovalStore


class PostgresApprovalStore(BaseApprovalStore):
    def __init__(self) -> None:
        self._initialized = False

    def list(self, limit: int = 50, status_filter: str | None = None) -> list[dict[str, Any]]:
        self._ensure_schema()
        with _LOCK, self._connect() as connection, connection.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cursor:
            if status_filter:
                cursor.execute(
                    "select payload_json from admin_agent_approvals where status = %s order by created_at desc limit %s",
                    (status_filter, limit),
                )
            else:
                cursor.execute("select payload_json from admin_agent_approvals order by created_at desc limit %s", (limit,))
            rows = cursor.fetchall()
        return [_decode_payload(row["payload_json"]) for row in rows]

    def get(self, approval_id: str) -> dict[str, Any] | None:
        self._ensure_schema()
        with _LOCK, self._connect() as connection, connection.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cursor:
            cursor.execute("select payload_json from admin_agent_approvals where id = %s", (approval_id,))
            row = cursor.fetchone()
        return _decode_payload(row["payload_json"]) if row else None

    def get_by_idempotency_key(self, idempotency_key: str) -> dict[str, Any] | None:
        self._ensure_schema()
        with _LOCK, self._connect() as connection, connection.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cursor:
            cursor.execute("select payload_json from admin_agent_approvals where idempotency_key = %s", (idempotency_key,))
            row = cursor.fetchone()
        return _decode_payload(row["payload_json"]) if row else None

    def save(self, row: dict[str, Any]) -> dict[str, Any]:
        self._ensure_schema()
        with _LOCK, self._connect() as connection, connection.cursor() as cursor:
            cursor.execute(_POSTGRES_UPSERT, _approval_values(row, self._payload_json(row)))
            cursor.execute("delete from admin_agent_approval_audit where approval_id = %s", (row["id"],))
            for index, event in enumerate(row.get("audit", [])):
                cursor.execute(_POSTGRES_AUDIT_INSERT, _audit_values(row["id"], index, event))
            connection.commit()
        return row

    def clear_for_tests(self) -> None:
        if settings.ADMIN_COPILOT_ENV != "test":
            raise RuntimeError("Refusing to clear Postgres approval store outside test environment")
        self._ensure_schema()
        with _LOCK, self._connect() as connection, connection.cursor() as cursor:
            cursor.execute("delete from admin_agent_approval_audit")
            cursor.execute("delete from admin_agent_approvals")
            connection.commit()

    def _connect(self):
        return psycopg2.connect(
            host=settings.APPROVAL_DB_HOST,
            port=settings.APPROVAL_DB_PORT,
            dbname=settings.APPROVAL_DB_NAME,
            user=settings.APPROVAL_DB_USERNAME,
            password=settings.APPROVAL_DB_PASSWORD,
            sslmode=settings.APPROVAL_DB_SSLMODE or None,
        )

    def _ensure_schema(self) -> None:
        if self._initialized:
            return
        if not settings.APPROVAL_DB_HOST or not settings.APPROVAL_DB_USERNAME:
            raise RuntimeError("Postgres approval store requires APPROVAL_DB_HOST and APPROVAL_DB_USERNAME")
        with _LOCK, self._connect() as connection, connection.cursor() as cursor:
            _create_schema(cursor)
            connection.commit()
        self._initialized = True


def build_approval_store() -> ApprovalStoreProtocol:
    if settings.APPROVAL_STORAGE_BACKEND.lower() == "postgres":
        return PostgresApprovalStore()
    return SQLiteApprovalStore()


def _create_schema(cursor: Any) -> None:
    cursor.execute(
        """
        create table if not exists admin_agent_approvals (
            id text primary key,
            action text not null,
            resource_type text not null,
            resource_id text not null,
            status text not null,
            idempotency_key text not null unique,
            payload_hash text not null,
            requested_by text not null,
            approved_by text,
            executed_by text,
            created_at text not null,
            updated_at text not null,
            expires_at text not null,
            payload_json text not null
        )
        """
    )
    cursor.execute(
        """
        create table if not exists admin_agent_approval_audit (
            approval_id text not null,
            event_index integer not null,
            event text not null,
            actor_id text not null,
            role text not null,
            occurred_at text not null,
            extra_json text not null,
            primary key (approval_id, event_index),
            foreign key (approval_id) references admin_agent_approvals(id)
        )
        """
    )
    cursor.execute("create index if not exists idx_admin_agent_approvals_status on admin_agent_approvals(status)")
    cursor.execute("create index if not exists idx_admin_agent_approvals_resource on admin_agent_approvals(resource_type, resource_id)")


def _approval_values(row: dict[str, Any], payload_json: str) -> tuple[Any, ...]:
    return (
        row["id"],
        row["action"],
        row["resourceType"],
        row["resourceId"],
        row["status"],
        row["idempotencyKey"],
        row["payloadHash"],
        row["requestedBy"],
        row.get("approvedBy"),
        row.get("executedBy"),
        row["createdAt"],
        row["updatedAt"],
        row["expiresAt"],
        payload_json,
    )


def _audit_values(approval_id: str, index: int, event: dict[str, Any]) -> tuple[Any, ...]:
    return (
        approval_id,
        index,
        event.get("event"),
        event.get("actorId"),
        event.get("role"),
        event.get("at"),
        json.dumps(event.get("extra", {}), sort_keys=True, separators=(",", ":")),
    )


def _decode_payload(value: Any) -> dict[str, Any]:
    if isinstance(value, dict):
        return value
    return json.loads(value)


_SQLITE_UPSERT = """
insert into admin_agent_approvals (
    id, action, resource_type, resource_id, status, idempotency_key,
    payload_hash, requested_by, approved_by, executed_by, created_at,
    updated_at, expires_at, payload_json
)
values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
on conflict(id) do update set
    action = excluded.action,
    resource_type = excluded.resource_type,
    resource_id = excluded.resource_id,
    status = excluded.status,
    idempotency_key = excluded.idempotency_key,
    payload_hash = excluded.payload_hash,
    requested_by = excluded.requested_by,
    approved_by = excluded.approved_by,
    executed_by = excluded.executed_by,
    updated_at = excluded.updated_at,
    expires_at = excluded.expires_at,
    payload_json = excluded.payload_json
"""

_POSTGRES_UPSERT = _SQLITE_UPSERT.replace("?", "%s").replace(
    "payload_json = excluded.payload_json",
    "payload_json = excluded.payload_json",
)

_SQLITE_AUDIT_INSERT = """
insert into admin_agent_approval_audit (
    approval_id, event_index, event, actor_id, role, occurred_at, extra_json
)
values (?, ?, ?, ?, ?, ?, ?)
"""

_POSTGRES_AUDIT_INSERT = _SQLITE_AUDIT_INSERT.replace("?", "%s")

approval_store = build_approval_store()
