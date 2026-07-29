package com.dunghaiquyen.ecommerce.modules.demand.dto;

import java.time.LocalDate;
import java.util.UUID;

public record DemandClassificationResponse(
        UUID variantId,
        String sku,
        String productName,
        String dataSource,
        LocalDate fromDate,
        LocalDate toDate,
        int historyDays,
        int nonZeroDays,
        long totalUnits,
        double adi,
        double cvSquared,
        double trendSlope,
        DemandPattern classification,
        DemandConfidence confidence,
        String reason,
        String algorithmVersion) {
}
