package com.dunghaiquyen.ecommerce.modules.orchestration.dto;

import java.time.Instant;
import java.time.LocalDate;

public record AiDataFreshnessResponse(
        String dataSource,
        Instant checkedAt,
        LocalDate latestSalesDate,
        Instant latestForecastGeneratedAt,
        Instant latestEvaluationUpdatedAt,
        Instant latestRecommendationCreatedAt,
        long salesRows,
        long forecastRows,
        long evaluationRows,
        long pendingRecommendationRows,
        boolean stale,
        long staleAfterMinutes
) {
}
