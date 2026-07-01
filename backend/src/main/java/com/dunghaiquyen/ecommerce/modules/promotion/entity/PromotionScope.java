package com.dunghaiquyen.ecommerce.modules.promotion.entity;

/**
 * CATEGORY is recognized by the V3 schema's check constraint but deliberately
 * not supported by CouponService yet (Phase N2 scope decision - see
 * CouponService class javadoc): a coupon whose promotion has this scope is
 * rejected at apply time with a clear 422, not silently mishandled.
 */
public enum PromotionScope {
    ORDER,
    PRODUCT,
    CATEGORY
}
