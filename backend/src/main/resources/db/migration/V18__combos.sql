-- Combo (bundle) discounts: buy a fixed set of specific products together and a
-- fixed amount is taken off the order. Independent of the per-variant sale price
-- (price/compare_at_price), which stays the product-level discount.
--
-- A combo applies to an order when the cart contains every product listed in
-- combo_products. The discount is combos.discount_amount (a flat VND amount).

create table combos (
    id              uuid            primary key,
    name            varchar(200)    not null,
    description     text,
    discount_amount numeric(12, 2)  not null,
    status          varchar(20)     not null default 'ACTIVE',
    created_at      timestamptz     not null,
    updated_at      timestamptz     not null,
    constraint chk_combos_status check (status in ('ACTIVE', 'INACTIVE')),
    constraint chk_combos_discount_nonneg check (discount_amount >= 0)
);

create table combo_products (
    id         uuid        primary key,
    combo_id   uuid        not null references combos (id) on delete cascade,
    product_id uuid        not null references products (id) on delete cascade,
    quantity   int         not null default 1,
    created_at timestamptz not null,
    constraint uq_combo_products unique (combo_id, product_id),
    constraint chk_combo_products_qty check (quantity >= 1)
);

create index idx_combo_products_combo on combo_products (combo_id);
create index idx_combo_products_product on combo_products (product_id);
