package com.dunghaiquyen.ecommerce.modules.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID productId,
        UUID variantId,
        String productName,
        String sku,
        String size,
        String color,
        String thumbnail,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal) {
}
