alter table orders drop constraint if exists chk_orders_order_status;

alter table orders add constraint chk_orders_order_status check (order_status in
    ('PENDING_CONFIRMATION', 'CANCELLATION_REQUESTED', 'CANCELLATION_APPROVED',
     'CONFIRMED', 'PACKING', 'SHIPPING', 'DELIVERED', 'CANCELLED'));
