package com.dunghaiquyen.ecommerce.modules.checkout.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * canCheckout is the single source of truth for "would POST /api/v1/orders
 * succeed right now with this exact cart/coupon": true only when every item
 * is valid AND (no coupon was requested OR the requested coupon is valid).
 * couponError is populated instead of failing the whole request when a
 * coupon is invalid - a bad/expired coupon code is something the customer
 * can simply remove and still complete checkout with everything else,
 * unlike an invalid cart item.
 */
public record CheckoutPreviewResponse(
        List<CheckoutItemPreview> items,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal shippingFee,
        BigDecimal totalAmount,
        AppliedCouponSummary appliedCoupon,
        String couponError,
        boolean canCheckout) {
}
