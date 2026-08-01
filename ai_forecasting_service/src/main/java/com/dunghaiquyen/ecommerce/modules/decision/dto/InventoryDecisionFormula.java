package com.dunghaiquyen.ecommerce.modules.decision.dto;

public record InventoryDecisionFormula(
        double averageDailyDemand,
        int leadTimeDays,
        int targetCoverDays,
        double residualStdDev,
        double serviceLevel,
        double zScore,
        int incomingQuantity,
        int expectedDemandDuringLeadTime,
        int safetyStock,
        int reorderPoint,
        int targetStock,
        int rawSuggestedQuantity,
        int minimumOrderQuantity,
        int packSize,
        int suggestedQuantity) {
}
