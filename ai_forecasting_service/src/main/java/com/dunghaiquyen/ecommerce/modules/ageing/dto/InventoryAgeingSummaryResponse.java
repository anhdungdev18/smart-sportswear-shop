package com.dunghaiquyen.ecommerce.modules.ageing.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record InventoryAgeingSummaryResponse(
        String dataSource,
        Instant generatedAt,
        int totalVariants,
        int variantsWithStock,
        int newNoSalesVariants,
        int watchVariants,
        int slowMovingVariants,
        int dormantVariants,
        int deadStockVariants,
        int variantsMissingSupplier,
        BigDecimal estimatedAtRiskValue,
        List<InventoryAgeingItemResponse> items) {
}
