-- Multi-color products: an image can belong to a specific colorway so the
-- storefront gallery shows the right photos when a color is selected.
-- Null = shared/generic image (applies to any color).
alter table product_images add column if not exists color varchar(120);
create index if not exists idx_product_images_product_color on product_images (product_id, color);
