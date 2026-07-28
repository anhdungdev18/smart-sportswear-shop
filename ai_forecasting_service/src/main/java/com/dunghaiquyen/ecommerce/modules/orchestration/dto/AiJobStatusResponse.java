package com.dunghaiquyen.ecommerce.modules.orchestration.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AiJobStatusResponse(
        String jobId,
        String status,
        String dataSource,
        Instant checkedAt,
        int requested,
        int processed,
        int succeeded,
        int failed,
        long durationMillis,
        List<UUID> failedVariantIds,
        List<String> warnings
) {
}
