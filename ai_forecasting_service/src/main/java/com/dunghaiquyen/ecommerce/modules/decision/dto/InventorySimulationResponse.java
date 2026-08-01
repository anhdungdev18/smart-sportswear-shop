package com.dunghaiquyen.ecommerce.modules.decision.dto;

import java.util.List;
import java.util.UUID;

public record InventorySimulationResponse(
        UUID variantId,
        InventoryRiskResponse current,
        InventoryRiskResponse simulated,
        int suggestedQuantityDelta,
        int reorderPointDelta,
        Integer stockoutDaysDelta,
        List<String> warnings) {
}
