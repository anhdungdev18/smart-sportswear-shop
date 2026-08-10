alter table orders add column if not exists cancellation_requested_by varchar(20);
alter table orders add column if not exists cancellation_reason text;
alter table orders add column if not exists cancellation_requested_at timestamptz;

alter table orders add constraint chk_orders_cancellation_requested_by
    check (cancellation_requested_by is null or cancellation_requested_by in ('CUSTOMER', 'STAFF'));

update orders
set cancellation_requested_by = 'CUSTOMER',
    cancellation_reason = coalesce(internal_note, 'Yêu cầu hủy và hoàn tiền'),
    cancellation_requested_at = updated_at
where order_status = 'CANCELLATION_REQUESTED'
  and cancellation_requested_by is null;
