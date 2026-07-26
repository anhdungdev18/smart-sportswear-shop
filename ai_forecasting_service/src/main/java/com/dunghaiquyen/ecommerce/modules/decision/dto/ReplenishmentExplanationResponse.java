package com.dunghaiquyen.ecommerce.modules.decision.dto;

import java.util.Map;
import java.util.UUID;

public record ReplenishmentExplanationResponse(
        UUID recommendationId,
        UUID variantId,
        InventoryRiskResponse decision,
        Map<String, Object> persistedExplanation) {
}
