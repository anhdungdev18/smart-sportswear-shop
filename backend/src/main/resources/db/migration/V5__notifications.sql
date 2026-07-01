-- Phase O (Notification module).
--
-- V3 already has an "email_logs" table, but it was never wired to any code
-- (no entity/repository anywhere) and its shape (template_code, payload_json,
-- provider_message_id, updated_at) does not match what this phase actually
-- needs (user_id/order_id/type/channel to drive admin filtering). Rather than
-- retrofit a table designed for something else, this adds a purpose-built
-- "notifications" table. email_logs is left exactly as-is - still unused,
-- not touched or populated by this phase.
create table notifications (
    id uuid primary key,
    user_id uuid references users (id) on delete set null,
    order_id uuid references orders (id) on delete set null,
    type varchar(30) not null,
    channel varchar(20) not null default 'EMAIL',
    recipient varchar(255) not null,
    subject varchar(255) not null,
    body text,
    status varchar(20) not null default 'PENDING',
    error_message text,
    created_at timestamptz not null,
    sent_at timestamptz,
    constraint chk_notifications_type check (
        type in ('ORDER_CREATED', 'ORDER_CANCELLED', 'ORDER_DELIVERED', 'PASSWORD_RESET')
    ),
    constraint chk_notifications_channel check (channel in ('EMAIL')),
    constraint chk_notifications_status check (status in ('PENDING', 'SENT', 'FAILED'))
);

create index idx_notifications_user_id on notifications (user_id);
create index idx_notifications_order_id on notifications (order_id);
create index idx_notifications_type on notifications (type);
create index idx_notifications_status on notifications (status);
create index idx_notifications_created_at on notifications (created_at);
