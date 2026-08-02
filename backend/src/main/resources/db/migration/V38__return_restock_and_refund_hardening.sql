alter table return_items add column if not exists restocked boolean not null default false;

alter table inventory_transactions drop constraint if exists chk_inventory_transactions_type;
alter table inventory_transactions add constraint chk_inventory_transactions_type check (type in
    ('IMPORT', 'EXPORT', 'ADJUSTMENT_UP', 'ADJUSTMENT_DOWN',
     'ORDER_RESERVE', 'ORDER_RELEASE', 'ORDER_CONFIRM_DEDUCT', 'RETURN_RESTOCK'));
