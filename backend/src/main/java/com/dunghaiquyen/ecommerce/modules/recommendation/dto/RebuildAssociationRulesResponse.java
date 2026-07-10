package com.dunghaiquyen.ecommerce.modules.recommendation.dto;

import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RebuildStatus;
import java.time.Instant;

public record RebuildAssociationRulesResponse(
        String modelVersion,
        RebuildStatus status,
        long totalTransactions,
        long totalRules,
        double minSupport,
        double minConfidence,
        double minLift,
        Instant startedAt,
        Instant finishedAt,
        String message
) {
}