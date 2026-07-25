package com.dunghaiquyen.ecommerce.modules.dataquality.repository;

import java.time.LocalDate;
import java.util.UUID;

public record SkuDataQualityRow(
        UUID variantId,
        String sku,
        String productName,
        int salesRows,
        int nonZeroDays,
        long totalUnits,
        LocalDate lastSaleDate,
        int inventorySnapshotDays,
        String supplierName) {
}
