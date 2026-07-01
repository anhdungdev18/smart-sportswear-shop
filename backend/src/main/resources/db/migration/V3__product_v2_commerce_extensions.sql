-- Product V2 commerce extensions.
-- Scope: promotions/coupons, shipping, returns/refunds, reviews, wishlists,
-- CMS-lite, and operational logs. This is schema-only: business logic and JPA
-- mappings can be phased in later without blocking the DB design.

-- ============================================================================
-- promotions
-- ============================================================================
create table promotions (
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

create index idx_promotions_status on promotions (status);
create index idx_promotions_starts_at on promotions (starts_at);
create index idx_promotions_ends_at on promotions (ends_at);
create index idx_promotions_created_by on promotions (created_by);

-- ============================================================================
-- promotion_rules
-- ============================================================================
create table promotion_rules (
    id uuid primary key,
    promotion_id uuid not null references promotions (id) on delete cascade,
    rule_type varchar(30) not null,
    rule_value varchar(255) not null,
    created_at timestamptz not null,
    constraint chk_promotion_rules_type check (
        rule_type in ('MIN_QUANTITY', 'TARGET_BRAND', 'TARGET_CATEGORY', 'TARGET_PRODUCT', 'TARGET_VARIANT')
    )
);

create index idx_promotion_rules_promotion_id on promotion_rules (promotion_id);

-- ============================================================================
-- promotion_products
-- ============================================================================
create table promotion_products (
    id uuid primary key,
    promotion_id uuid not null references promotions (id) on delete cascade,
    product_id uuid not null references products (id) on delete cascade,
    created_at timestamptz not null,
    constraint uq_promotion_products unique (promotion_id, product_id)
);

create index idx_promotion_products_product_id on promotion_products (product_id);

-- ============================================================================
-- coupons
-- ============================================================================
create table coupons (
    id uuid primary key,
    promotion_id uuid references promotions (id) on delete set null,
    code varchar(80) not null,
    description text,
    status varchar(20) not null default 'ACTIVE',
    starts_at timestamptz,
    ends_at timestamptz,
    usage_limit integer,
    usage_count integer not null default 0,
    per_user_limit integer,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_coupons_code unique (code),
    constraint chk_coupons_status check (status in ('ACTIVE', 'INACTIVE', 'EXPIRED')),
    constraint chk_coupons_usage_limit check (usage_limit is null or usage_limit > 0),
    constraint chk_coupons_usage_count check (usage_count >= 0),
    constraint chk_coupons_per_user_limit check (per_user_limit is null or per_user_limit > 0),
    constraint chk_coupons_time_range check (
        starts_at is null or ends_at is null or starts_at <= ends_at
    )
);

create index idx_coupons_promotion_id on coupons (promotion_id);
create index idx_coupons_status on coupons (status);

-- ============================================================================
-- coupon_usages
-- ============================================================================
create table coupon_usages (
    id uuid primary key,
    coupon_id uuid not null references coupons (id) on delete cascade,
    order_id uuid not null references orders (id) on delete cascade,
    user_id uuid not null references users (id) on delete cascade,
    discount_amount numeric(12, 2) not null,
    used_at timestamptz not null,
    constraint uq_coupon_usages_order_id unique (order_id),
    constraint chk_coupon_usages_discount_amount check (discount_amount >= 0)
);

create index idx_coupon_usages_coupon_id on coupon_usages (coupon_id);
create index idx_coupon_usages_user_id on coupon_usages (user_id);

-- ============================================================================
-- shipping_methods
-- ============================================================================
create table shipping_methods (
    id uuid primary key,
    name varchar(150) not null,
    code varchar(50) not null,
    description text,
    status varchar(20) not null default 'ACTIVE',
    provider varchar(50),
    base_fee numeric(12, 2) not null default 0,
    estimated_days_min integer,
    estimated_days_max integer,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_shipping_methods_code unique (code),
    constraint chk_shipping_methods_status check (status in ('ACTIVE', 'INACTIVE')),
    constraint chk_shipping_methods_base_fee check (base_fee >= 0),
    constraint chk_shipping_methods_eta_min check (estimated_days_min is null or estimated_days_min >= 0),
    constraint chk_shipping_methods_eta_max check (estimated_days_max is null or estimated_days_max >= 0),
    constraint chk_shipping_methods_eta_range check (
        estimated_days_min is null or estimated_days_max is null or estimated_days_min <= estimated_days_max
    )
);

-- ============================================================================
-- shipments
-- ============================================================================
create table shipments (
    id uuid primary key,
    order_id uuid not null references orders (id) on delete cascade,
    shipping_method_id uuid references shipping_methods (id),
    shipment_code varchar(50) not null,
    provider varchar(50),
    tracking_number varchar(120),
    status varchar(30) not null default 'PENDING',
    shipping_fee numeric(12, 2) not null default 0,
    receiver_name varchar(150) not null,
    receiver_phone varchar(20) not null,
    province varchar(100) not null,
    district varchar(100) not null,
    ward varchar(100) not null,
    address_line varchar(255) not null,
    shipped_at timestamptz,
    delivered_at timestamptz,
    note text,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_shipments_shipment_code unique (shipment_code),
    constraint uq_shipments_tracking_number unique (tracking_number),
    constraint chk_shipments_status check (
        status in ('PENDING', 'READY_TO_SHIP', 'SHIPPING', 'DELIVERED', 'FAILED', 'RETURNED', 'CANCELLED')
    ),
    constraint chk_shipments_shipping_fee check (shipping_fee >= 0),
    constraint chk_shipments_delivery_time check (
        shipped_at is null or delivered_at is null or shipped_at <= delivered_at
    )
);

create index idx_shipments_order_id on shipments (order_id);
create index idx_shipments_shipping_method_id on shipments (shipping_method_id);
create index idx_shipments_status on shipments (status);

-- ============================================================================
-- returns
-- ============================================================================
create table returns (
    id uuid primary key,
    order_id uuid not null references orders (id) on delete cascade,
    user_id uuid not null references users (id),
    return_code varchar(50) not null,
    status varchar(30) not null default 'REQUESTED',
    reason varchar(100) not null,
    description text,
    requested_at timestamptz not null,
    approved_at timestamptz,
    received_at timestamptz,
    resolved_at timestamptz,
    handled_by uuid references users (id),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_returns_return_code unique (return_code),
    constraint chk_returns_status check (
        status in ('REQUESTED', 'APPROVED', 'REJECTED', 'RECEIVED', 'REFUNDED', 'CANCELLED')
    ),
    constraint chk_returns_reason check (
        reason in ('WRONG_SIZE', 'DAMAGED', 'DEFECTIVE', 'WRONG_ITEM', 'NO_LONGER_NEEDED', 'OTHER')
    )
);

create index idx_returns_order_id on returns (order_id);
create index idx_returns_user_id on returns (user_id);
create index idx_returns_handled_by on returns (handled_by);

-- ============================================================================
-- return_items
-- ============================================================================
create table return_items (
    id uuid primary key,
    return_id uuid not null references returns (id) on delete cascade,
    order_item_id uuid not null references order_items (id),
    quantity integer not null,
    reason varchar(100),
    condition_status varchar(30),
    resolution varchar(30),
    refund_amount numeric(12, 2),
    created_at timestamptz not null,
    constraint chk_return_items_quantity check (quantity > 0),
    constraint chk_return_items_condition_status check (
        condition_status is null or condition_status in ('UNOPENED', 'LIKE_NEW', 'USED', 'DAMAGED')
    ),
    constraint chk_return_items_resolution check (
        resolution is null or resolution in ('REFUND', 'EXCHANGE', 'STORE_CREDIT', 'REJECT')
    ),
    constraint chk_return_items_refund_amount check (refund_amount is null or refund_amount >= 0)
);

create index idx_return_items_return_id on return_items (return_id);
create index idx_return_items_order_item_id on return_items (order_item_id);

-- ============================================================================
-- refunds
-- ============================================================================
create table refunds (
    id uuid primary key,
    return_id uuid references returns (id) on delete set null,
    order_id uuid not null references orders (id) on delete cascade,
    payment_id uuid references payments (id) on delete set null,
    refund_code varchar(50) not null,
    amount numeric(12, 2) not null,
    provider varchar(20) not null,
    status varchar(20) not null default 'PENDING',
    reason text,
    refunded_at timestamptz,
    created_by uuid references users (id),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_refunds_refund_code unique (refund_code),
    constraint chk_refunds_amount check (amount >= 0),
    constraint chk_refunds_provider check (provider in ('COD', 'VNPAY', 'MANUAL')),
    constraint chk_refunds_status check (status in ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED'))
);

create index idx_refunds_return_id on refunds (return_id);
create index idx_refunds_order_id on refunds (order_id);
create index idx_refunds_payment_id on refunds (payment_id);

-- ============================================================================
-- product_reviews
-- ============================================================================
create table product_reviews (
    id uuid primary key,
    product_id uuid not null references products (id) on delete cascade,
    user_id uuid not null references users (id) on delete cascade,
    order_item_id uuid references order_items (id) on delete set null,
    rating integer not null,
    title varchar(200),
    content text,
    status varchar(20) not null default 'PENDING',
    is_verified_purchase boolean not null default false,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_product_reviews_rating check (rating between 1 and 5),
    constraint chk_product_reviews_status check (status in ('PENDING', 'APPROVED', 'REJECTED', 'HIDDEN'))
);

create index idx_product_reviews_product_id on product_reviews (product_id);
create index idx_product_reviews_user_id on product_reviews (user_id);
create index idx_product_reviews_status on product_reviews (status);

-- ============================================================================
-- wishlists
-- ============================================================================
create table wishlists (
    id uuid primary key,
    user_id uuid not null references users (id) on delete cascade,
    name varchar(150) not null default 'Yeu thich',
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_wishlists_user_id unique (user_id)
);

-- ============================================================================
-- wishlist_items
-- ============================================================================
create table wishlist_items (
    id uuid primary key,
    wishlist_id uuid not null references wishlists (id) on delete cascade,
    product_id uuid not null references products (id) on delete cascade,
    created_at timestamptz not null,
    constraint uq_wishlist_items unique (wishlist_id, product_id)
);

create index idx_wishlist_items_product_id on wishlist_items (product_id);

-- ============================================================================
-- banners
-- ============================================================================
create table banners (
    id uuid primary key,
    name varchar(180) not null,
    code varchar(80) not null,
    placement varchar(50) not null,
    status varchar(20) not null default 'DRAFT',
    starts_at timestamptz,
    ends_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_banners_code unique (code),
    constraint chk_banners_placement check (
        placement in ('HOME_HERO', 'HOME_GRID', 'COLLECTION_TOP', 'SIDEBAR', 'POPUP', 'FOOTER')
    ),
    constraint chk_banners_status check (status in ('DRAFT', 'ACTIVE', 'INACTIVE')),
    constraint chk_banners_time_range check (
        starts_at is null or ends_at is null or starts_at <= ends_at
    )
);

create index idx_banners_status on banners (status);

-- ============================================================================
-- banner_items
-- ============================================================================
create table banner_items (
    id uuid primary key,
    banner_id uuid not null references banners (id) on delete cascade,
    title varchar(180),
    subtitle varchar(255),
    image_url varchar(500) not null,
    target_url varchar(500),
    product_id uuid references products (id) on delete set null,
    sort_order integer not null default 0,
    is_active boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_banner_items_banner_id on banner_items (banner_id);
create index idx_banner_items_product_id on banner_items (product_id);

-- ============================================================================
-- pages
-- ============================================================================
create table pages (
    id uuid primary key,
    title varchar(200) not null,
    slug varchar(220) not null,
    summary varchar(500),
    content_html text not null,
    status varchar(20) not null default 'DRAFT',
    published_at timestamptz,
    created_by uuid references users (id),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_pages_slug unique (slug),
    constraint chk_pages_status check (status in ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

create index idx_pages_status on pages (status);
create index idx_pages_created_by on pages (created_by);

-- ============================================================================
-- site_settings
-- ============================================================================
create table site_settings (
    id uuid primary key,
    setting_key varchar(120) not null,
    setting_value text,
    value_type varchar(20) not null default 'STRING',
    description text,
    is_public boolean not null default false,
    updated_by uuid references users (id),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_site_settings_key unique (setting_key),
    constraint chk_site_settings_value_type check (
        value_type in ('STRING', 'NUMBER', 'BOOLEAN', 'JSON')
    )
);

create index idx_site_settings_updated_by on site_settings (updated_by);

-- ============================================================================
-- audit_logs
-- ============================================================================
create table audit_logs (
    id uuid primary key,
    actor_user_id uuid references users (id) on delete set null,
    action varchar(100) not null,
    entity_type varchar(100) not null,
    entity_id varchar(100) not null,
    before_json jsonb,
    after_json jsonb,
    ip_address varchar(64),
    user_agent varchar(500),
    created_at timestamptz not null
);

create index idx_audit_logs_actor_user_id on audit_logs (actor_user_id);
create index idx_audit_logs_entity on audit_logs (entity_type, entity_id);
create index idx_audit_logs_created_at on audit_logs (created_at);

-- ============================================================================
-- email_logs
-- ============================================================================
create table email_logs (
    id uuid primary key,
    recipient_email varchar(255) not null,
    subject varchar(255) not null,
    template_code varchar(100),
    payload_json jsonb,
    status varchar(20) not null default 'PENDING',
    provider_message_id varchar(150),
    error_message text,
    sent_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint chk_email_logs_status check (status in ('PENDING', 'SENT', 'FAILED', 'CANCELLED'))
);

create index idx_email_logs_recipient_email on email_logs (recipient_email);
create index idx_email_logs_status on email_logs (status);
create index idx_email_logs_sent_at on email_logs (sent_at);
