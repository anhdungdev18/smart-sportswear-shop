-- Phase N1 (Wishlist + Product Reviews).
--
-- V3 already enforces "no duplicate product in the same wishlist" at the DB
-- level (uq_wishlist_items on wishlist_items). It left product_reviews
-- without an equivalent uniqueness guard, so this adds the one this phase
-- needs: a user may review the SAME order_item at most once. Reviewing is
-- scoped per order_item (not per product) on purpose - see ReviewService's
-- class javadoc for the full reasoning. order_item_id stays nullable (per
-- V3), so this constraint only actually bites once an order_item is set,
-- which is the only path this phase's API creates reviews through.
alter table product_reviews
    add constraint uq_product_reviews_user_order_item unique (user_id, order_item_id);
