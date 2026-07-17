package com.dunghaiquyen.ecommerce.modules.checkout.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * canCheckout is the single source of truth for "would POST /api/v1/orders
 * succeed right now with this exact cart": true only when every item is valid.
 * discountAmount is the combo (bundle) discount that would apply to this cart.
 */
public record CheckoutPreviewResponse(
        List<CheckoutItemPreview> items,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal shippingFee,
        BigDecimal totalAmount,
        boolean canCheckout) {
}
