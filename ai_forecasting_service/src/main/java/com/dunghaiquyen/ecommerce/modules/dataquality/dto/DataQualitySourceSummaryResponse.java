package com.dunghaiquyen.ecommerce.modules.dataquality.dto;

public record DataQualitySourceSummaryResponse(
        String dataSource,
        int totalVariants,
        int highQualityVariants,
        int mediumQualityVariants,
        int lowQualityVariants,
        int insufficientVariants,
        int variantsMissingSupplier,
        int variantsWithMissingSalesDays,
        int variantsWithInventoryGaps) {
}
