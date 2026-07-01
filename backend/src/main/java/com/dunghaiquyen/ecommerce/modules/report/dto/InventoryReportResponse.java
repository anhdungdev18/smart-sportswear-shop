package com.dunghaiquyen.ecommerce.modules.report.dto;

import java.util.List;

public record InventoryReportResponse(
        long totalVariants,
        long totalStockQuantity,
        long totalReservedQuantity,
        long totalAvailableQuantity,
        int lowStockThreshold,
        long lowStockCount,
        List<LowStockItemResponse> lowStockItems) {
}
