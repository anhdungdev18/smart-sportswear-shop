-- Customer VAT-style invoice request (company name/tax code/address, captured
-- at checkout) plus a persisted invoice number issued once the order is
-- actually eligible for invoicing (paid, not cancelled) - see
-- OrderService.getOrderInvoice.
alter table orders
    add column invoice_number varchar(30),
    add column invoice_requested boolean not null default false,
    add column invoice_company_name varchar(200),
    add column invoice_tax_code varchar(50),
    add column invoice_company_address varchar(255);

alter table orders
    add constraint uq_orders_invoice_number unique (invoice_number);

create sequence orders_invoice_number_seq start with 1;
