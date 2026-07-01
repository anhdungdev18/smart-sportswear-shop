package com.dunghaiquyen.ecommerce.modules.shipping.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Deliberately no couponCode field here: no promotion type in this codebase
 * makes a coupon affect shippingFee (coupons only discount subtotal/total -
 * see CouponService), so accepting one on this endpoint would be a dead
 * parameter that silently does nothing - judged worse than just not
 * accepting it. A coupon's effect is already fully visible via the existing
 * POST /api/v1/checkout/preview.
 */
public record ShippingFeePreviewRequest(@NotNull UUID addressId, @NotNull UUID shippingMethodId) {
}
