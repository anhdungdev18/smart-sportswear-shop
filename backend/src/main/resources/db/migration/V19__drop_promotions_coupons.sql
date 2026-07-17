-- Promotions/coupons were removed from the product scope.
-- The remaining discount model is:
-- 1. Fixed sale price at variant level (price/compare_at_price).
-- 2. Fixed-amount combo discount when the cart contains all products in a combo.

drop table if exists coupon_usages cascade;
drop table if exists coupons cascade;
drop table if exists promotion_products cascade;
drop table if exists promotion_rules cascade;
drop table if exists promotions cascade;
