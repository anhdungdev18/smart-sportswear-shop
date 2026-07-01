-- At most one primary image per product (ERD_PHASE1.md 6.9). The previous
-- enforcement was service-layer only (read current primary, unset it, then
-- insert the new one) - a classic read-modify-write race: two concurrent
-- "add primary image" requests for the same product can both read "no
-- conflicting primary yet" and both insert with is_primary = true, leaving
-- two primary rows. Only a DB constraint closes that window for good.
create unique index uq_product_images_primary on product_images (product_id) where is_primary = true;
