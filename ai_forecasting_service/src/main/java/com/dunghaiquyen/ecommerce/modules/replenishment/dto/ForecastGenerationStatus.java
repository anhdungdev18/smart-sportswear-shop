package com.dunghaiquyen.ecommerce.modules.replenishment.dto;

import java.util.List;
import java.util.UUID;

public record ForecastGenerationStatus(
        Status status,
        int requested,
        int processed,
        int succeeded,
        int failed,
        long durationMillis,
        List<UUID> failedVariantIds
) {
    public enum Status {
        IDLE,
        SYNCING,
        EVALUATING,
        FORECASTING,
        COMPLETED,
        FAILED
    }
}