package com.dunghaiquyen.ecommerce.modules.recommendation.dto;

import java.time.Instant;
import java.util.List;

public record RecommendationLogSummaryResponse(
        Instant from,
        Instant to,
        long totalLogs,
        long impressions,
        long clicks,
        long addToCarts,
        List<RecommendationLogProductStatsResponse> topProducts
) {
}