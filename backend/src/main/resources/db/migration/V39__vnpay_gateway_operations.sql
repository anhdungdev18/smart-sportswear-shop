alter table payments add column if not exists transaction_date varchar(14);
alter table payments add column if not exists expires_at timestamptz;
alter table payments add column if not exists gateway_transaction_no varchar(30);
alter table payments add column if not exists bank_code varchar(30);

alter table refunds add column if not exists gateway_request_id varchar(32);
alter table refunds add column if not exists gateway_transaction_no varchar(30);
alter table refunds add column if not exists gateway_response_json jsonb;

create unique index if not exists uq_refunds_gateway_request_id
    on refunds (gateway_request_id) where gateway_request_id is not null;
