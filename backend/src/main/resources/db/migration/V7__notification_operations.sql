-- Notification Operations phase.
--
-- V1-V6 are left untouched. Two additive changes only:
--
-- 1. notification_templates: lets admin edit the subject/body of each
--    NotificationType's email (named {placeholder} tokens substituted at
--    send time - see NotificationTemplates.java). One row per type; rows are
--    seeded at application startup (NotificationTemplateSeeder), not by this
--    migration, so the Vietnamese default copy stays in Java source (already
--    proven to round-trip correctly through Postgres) rather than embedded
--    in a .sql file.
--
-- 2. notifications.resend_of_id / resend_count / last_resend_at: support
--    POST /api/v1/admin/notifications/{id}/resend. A resend creates a NEW
--    notification row (resend_of_id pointing back to the original) rather
--    than mutating the original - see NotificationService.resend's javadoc.
--    resend_count/last_resend_at live on the ORIGINAL row only and back the
--    max-attempts and cooldown rules that keep resend from being abused.
create table notification_templates (
    id uuid primary key,
    type varchar(30) not null,
    channel varchar(20) not null default 'EMAIL',
    subject varchar(255) not null,
    body text not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_notification_templates_type unique (type),
    constraint chk_notification_templates_type check (
        type in ('ORDER_CREATED', 'ORDER_CANCELLED', 'ORDER_DELIVERED', 'ORDER_SHIPPING', 'PASSWORD_RESET')
    ),
    constraint chk_notification_templates_channel check (channel in ('EMAIL'))
);

alter table notifications add column resend_of_id uuid references notifications (id) on delete set null;
alter table notifications add column resend_count integer not null default 0;
alter table notifications add column last_resend_at timestamptz;

create index idx_notifications_resend_of_id on notifications (resend_of_id);
