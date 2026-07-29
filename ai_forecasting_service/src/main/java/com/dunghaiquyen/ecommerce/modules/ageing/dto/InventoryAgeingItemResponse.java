package com.dunghaiquyen.ecommerce.modules.ageing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InventoryAgeingItemResponse(
        UUID variantId,
        UUID productId,
        String sku,
        String productName,
        String size,
        String color,
        int availableQuantity,
        BigDecimal unitPrice,
        BigDecimal estimatedInventoryValue,
        LocalDate lastImportDate,
        LocalDate lastSaleDate,
        int inventoryAgeDays,
        int daysWithoutSale,
        long unitsSold30Days,
        long unitsSold90Days,
        long unitsSold180Days,
        InventoryAgeingStatus status,
        int urgencyScore,
        boolean supplierConfigured,
        List<String> recommendedActions) {
}
