-- Add the order inbox events introduced for administrators and cancellation approval.
-- Existing migrations remain immutable; both persisted notification tables enforce
-- NotificationType with CHECK constraints and therefore must evolve together.
alter table notifications drop constraint chk_notifications_type;
alter table notifications add constraint chk_notifications_type check (
    type in (
        'ORDER_CREATED', 'ORDER_CANCELLED', 'ORDER_DELIVERED', 'ORDER_SHIPPING',
        'ADMIN_ORDER_CREATED', 'ADMIN_ORDER_CANCELLED', 'CANCELLATION_APPROVED',
        'PASSWORD_RESET'
    )
);

alter table notification_templates drop constraint chk_notification_templates_type;
alter table notification_templates add constraint chk_notification_templates_type check (
    type in (
        'ORDER_CREATED', 'ORDER_CANCELLED', 'ORDER_DELIVERED', 'ORDER_SHIPPING',
        'ADMIN_ORDER_CREATED', 'ADMIN_ORDER_CANCELLED', 'CANCELLATION_APPROVED',
        'PASSWORD_RESET'
    )
);
