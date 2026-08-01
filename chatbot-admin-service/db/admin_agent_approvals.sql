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
);

create index if not exists idx_admin_agent_approvals_status
    on admin_agent_approvals(status);

create index if not exists idx_admin_agent_approvals_resource
    on admin_agent_approvals(resource_type, resource_id);

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
);
