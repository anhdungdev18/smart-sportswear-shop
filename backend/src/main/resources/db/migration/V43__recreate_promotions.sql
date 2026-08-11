-- V19 dropped promotions/promotion_products/coupons ("removed from product
-- scope"). This feature re-introduces product-percentage promotions with a
-- start/end window (admin sets a discount % + starts_at/ends_at for a set of
-- products; ProductService applies it automatically while live). Coupons and
-- promotion_rules are deliberately NOT recreated - out of scope here.
--
-- "if not exists" because this shared DB currently has these two tables
-- present out-of-band (created outside Flyway after V19 ran) - this migration
-- makes that state reproducible from a clean database instead of relying on
-- it, without erroring out on the DB where they already exist.

create table if not exists promotions (
    id uuid primary key,
    name varchar(200) not null,
    slug varchar(220) not null,
    description text,
    type varchar(30) not null,
    scope varchar(30) not null,
    status varchar(20) not null default 'DRAFT',
    discount_percent numeric(5, 2),
    discount_amount numeric(12, 2),
    min_order_amount numeric(12, 2),
    max_discount_amount numeric(12, 2),
    starts_at timestamptz,
    ends_at timestamptz,
    usage_limit integer,
    usage_count integer not null default 0,
    created_by uuid references users (id),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_promotions_slug unique (slug),
    constraint chk_promotions_type check (type in ('PERCENTAGE', 'FIXED_AMOUNT')),
    constraint chk_promotions_scope check (scope in ('ORDER', 'PRODUCT', 'CATEGORY')),
    constraint chk_promotions_status check (status in ('DRAFT', 'ACTIVE', 'INACTIVE', 'EXPIRED')),
    constraint chk_promotions_discount_percent check (
        discount_percent is null or (discount_percent > 0 and discount_percent <= 100)
    ),
    constraint chk_promotions_discount_amount check (
        discount_amount is null or discount_amount > 0
    ),
    constraint chk_promotions_min_order_amount check (
        min_order_amount is null or min_order_amount >= 0
    ),
    constraint chk_promotions_max_discount_amount check (
        max_discount_amount is null or max_discount_amount > 0
    ),
    constraint chk_promotions_usage_limit check (usage_limit is null or usage_limit > 0),
    constraint chk_promotions_usage_count check (usage_count >= 0),
    constraint chk_promotions_time_range check (
        starts_at is null or ends_at is null or starts_at <= ends_at
    ),
    constraint chk_promotions_value_presence check (
        (type = 'PERCENTAGE' and discount_percent is not null and discount_amount is null) or
        (type = 'FIXED_AMOUNT' and discount_amount is not null and discount_percent is null)
    )
);

create table if not exists promotion_products (
    id uuid primary key,
    promotion_id uuid not null references promotions (id) on delete cascade,
    product_id uuid not null references products (id) on delete cascade,
    created_at timestamptz not null,
    constraint uq_promotion_products unique (promotion_id, product_id)
);

create index if not exists idx_promotions_active_window
    on promotions (status, type, scope, starts_at, ends_at);

create index if not exists idx_promotion_products_promotion
    on promotion_products (promotion_id);

create index if not exists idx_promotion_products_product
    on promotion_products (product_id);
