-- Performance indexes for common sort/filter patterns

-- Products: default sort is created_at DESC, featured filter used in promotions
create index if not exists idx_products_created_at on products (created_at desc);
create index if not exists idx_products_featured on products (is_featured);
create index if not exists idx_products_status_created_at on products (status, created_at desc);

-- Orders: admin listing sorted by created_at DESC
create index if not exists idx_orders_created_at on orders (created_at desc);
create index if not exists idx_orders_status_created_at on orders (order_status, created_at desc);

-- Promotions: queried by scope + status for active discount lookups
create index if not exists idx_promotions_scope_status on promotions (scope, status);
create index if not exists idx_promotions_status on promotions (status);

-- Inventory: stock lives on product_variants, especially for low-stock / available queries
create index if not exists idx_product_variants_available_quantity
    on product_variants ((stock_quantity - reserved_quantity));
create index if not exists idx_product_variants_status_available_quantity
    on product_variants (status, (stock_quantity - reserved_quantity));

-- Banners: queried by placement + status
create index if not exists idx_banners_placement_status on banners (placement, status);

-- Reviews: admin listing filtered by status
create index if not exists idx_product_reviews_status on product_reviews (status);
create index if not exists idx_product_reviews_created_at on product_reviews (created_at desc);
