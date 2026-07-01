-- Catalog V2: product_type + collections + product_collections (Phase P).
--
-- V1-V9 untouched. Three additive changes only:
--
-- 1. products.product_type (nullable): backward-compatible with every existing
--    product row; existing records get NULL. The check constraint enforces
--    the four-value set when a value IS present. Nullable also means the
--    existing admin create/update API continues to work without breaking any
--    client that does not yet send the field.
--
-- 2. collections: marketing/merchandising groupings independent of categories.
--    A collection spans product types (e.g. "BST Mùa hè 2026" can hold
--    APPAREL, FOOTWEAR, ACCESSORY in the same collection).
--
-- 3. product_collections: many-to-many join table with deduplication
--    enforced at DB level (uq_product_collections).

-- ============================================================================
-- 1. products.product_type
-- ============================================================================
alter table products add column product_type varchar(30);

alter table products add constraint chk_products_product_type check (
    product_type is null or product_type in ('APPAREL', 'FOOTWEAR', 'ACCESSORY', 'EQUIPMENT')
);

create index idx_products_product_type on products (product_type);

-- ============================================================================
-- 2. collections
-- ============================================================================
create table collections (
    id uuid primary key,
    name varchar(200) not null,
    slug varchar(220) not null,
    description text,
    short_description varchar(500),
    collection_type varchar(30) not null,
    season varchar(50),
    year integer,
    banner_image_url varchar(500),
    cover_image_url varchar(500),
    status varchar(20) not null default 'DRAFT',
    starts_at timestamptz,
    ends_at timestamptz,
    sort_order integer not null default 0,
    is_featured boolean not null default false,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uq_collections_slug unique (slug),
    constraint chk_collections_type check (
        collection_type in ('SEASONAL', 'SPORT', 'CAMPAIGN', 'CAPSULE', 'NEW_ARRIVAL')
    ),
    constraint chk_collections_status check (
        status in ('DRAFT', 'ACTIVE', 'INACTIVE', 'ARCHIVED')
    ),
    constraint chk_collections_time_range check (
        starts_at is null or ends_at is null or starts_at <= ends_at
    )
);

create index idx_collections_status on collections (status);
create index idx_collections_is_featured on collections (is_featured);
create index idx_collections_sort_order on collections (sort_order);

-- ============================================================================
-- 3. product_collections
-- ============================================================================
create table product_collections (
    id uuid primary key,
    product_id uuid not null references products (id) on delete cascade,
    collection_id uuid not null references collections (id) on delete cascade,
    sort_order integer not null default 0,
    is_primary boolean not null default false,
    created_at timestamptz not null,
    constraint uq_product_collections unique (product_id, collection_id)
);

create index idx_product_collections_product_id on product_collections (product_id);
create index idx_product_collections_collection_id on product_collections (collection_id);
