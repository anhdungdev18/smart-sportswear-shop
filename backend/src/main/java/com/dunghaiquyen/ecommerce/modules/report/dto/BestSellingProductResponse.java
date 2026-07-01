package com.dunghaiquyen.ecommerce.modules.report.dto;

import java.math.BigDecimal;
import java.util.UUID;

/** Built via a JPQL constructor expression aggregating real order_items - never cart or reservation data. */
public record BestSellingProductResponse(
        UUID productId, String productName, Long totalQuantitySold, BigDecimal totalRevenue) {
}
