package com.dunghaiquyen.ecommerce.modules.checkout.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * One unified list rather than separate "valid items" / "invalid items"
 * arrays - every cart line appears exactly once, with `valid` and
 * `errorMessage` telling the caller which bucket it is in. Two parallel
 * arrays would raise an awkward question (could a line ever appear in both,
 * or neither?) that this shape makes structurally impossible to even ask.
 */
public record CheckoutItemPreview(
        UUID variantId,
        UUID productId,
        String productName,
        String sku,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        boolean valid,
        String errorMessage) {
}
