package com.dunghaiquyen.ecommerce.modules.cart.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID variantId,
        UUID productId,
        String productName,
        String sku,
        String size,
        String color,
        BigDecimal price,
        int quantity,
        BigDecimal lineTotal,
        String thumbnail) {
}
