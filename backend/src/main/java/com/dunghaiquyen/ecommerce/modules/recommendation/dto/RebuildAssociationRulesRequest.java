package com.dunghaiquyen.ecommerce.modules.recommendation.dto;

public record RebuildAssociationRulesRequest(
        Double minSupport,
        Double minConfidence,
        Double minLift,
        Integer minTransactions
) {
}