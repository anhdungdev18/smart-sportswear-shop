package com.dunghaiquyen.ecommerce.modules.checkout.dto;

import java.math.BigDecimal;

/**
 * Always a 200 - never an exception - for every business-invalid case
 * (unknown/expired/inactive code, usage limit, min order amount, cart empty
 * or has invalid lines). discountAmount is the SAME number
 * POST /api/v1/orders would apply right now if this exact coupon were sent
 * with this exact cart (both read off CouponService.validate's
 * AppliedCoupon - see CheckoutPreviewService.validateCoupon), so it is only
 * an "estimate" in the sense that the cart/coupon state can still change
 * between this call and an actual checkout (stock, usage limit racing
 * another customer, etc.) - not because a different formula is used.
 */
public record CouponValidationResponse(
        boolean valid, String couponCode, BigDecimal subtotal, BigDecimal discountAmount, String message) {
}
