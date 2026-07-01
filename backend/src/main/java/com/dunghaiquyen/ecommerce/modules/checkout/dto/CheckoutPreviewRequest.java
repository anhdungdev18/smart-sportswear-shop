package com.dunghaiquyen.ecommerce.modules.checkout.dto;

import java.util.UUID;

/**
 * Both fields optional. addressId is only checked for ownership if provided
 * (Phase 1 has no address-dependent shipping rule, so a preview without one
 * still produces a fully usable subtotal/shipping/total) - couponCode is
 * validated by the exact same CouponService used at real checkout.
 */
public record CheckoutPreviewRequest(UUID addressId, String couponCode) {
}
