package com.dunghaiquyen.ecommerce.modules.recommendation.dto;

import java.util.UUID;

public record RecommendationLogProductStatsResponse(
        UUID productId,
        String productName,
        long impressions,
        long clicks,
        long addToCarts,
        double clickThroughRatePercent
) {
}