package com.dunghaiquyen.ecommerce.modules.decision.dto;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastConfidence;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InventoryRiskResponse(
        UUID variantId,
        UUID productId,
        String sku,
        String productName,
        String size,
        String color,
        InventoryRiskType risk,
        InventoryRiskLevel severity,
        int availableQuantity,
        int incomingQuantity,
        int expectedDemandDuringLeadTime,
        int safetyStock,
        int reorderPoint,
        int suggestedQuantity,
        Integer estimatedStockoutDays,
        Integer estimatedStockoutDateOffsetDays,
        Double stockoutProbability,
        OverstockMetrics overstock,
        ForecastConfidence confidence,
        String selectedModel,
        String demandPattern,
        InventoryDecisionFormula formula,
        List<String> reasons,
        List<String> warnings,
        Instant generatedAt) {
}
