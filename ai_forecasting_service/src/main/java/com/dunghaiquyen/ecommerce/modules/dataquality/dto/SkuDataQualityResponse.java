package com.dunghaiquyen.ecommerce.modules.dataquality.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SkuDataQualityResponse(
        UUID variantId,
        String sku,
        String productName,
        String dataSource,
        LocalDate fromDate,
        LocalDate toDate,
        int historyDays,
        int nonZeroDays,
        long totalUnits,
        int missingDays,
        Integer daysSinceLastSale,
        int inventorySnapshotDays,
        boolean supplierConfigured,
        int qualityScore,
        SkuDataQualityLevel qualityLevel,
        List<String> warnings) {
}
