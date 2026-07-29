package com.dunghaiquyen.ecommerce.modules.dataquality.dto;

import java.util.List;

public record DataQualitySummaryResponse(
        int totalVariants,
        int highQualityVariants,
        int mediumQualityVariants,
        int lowQualityVariants,
        int insufficientVariants,
        int variantsMissingSupplier,
        int variantsWithMissingSalesDays,
        int variantsWithInventoryGaps,
        List<DataQualitySourceSummaryResponse> bySource) {
}
