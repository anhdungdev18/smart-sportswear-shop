package com.dunghaiquyen.ecommerce.modules.decision.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record InventorySimulationRequest(
        @NotNull UUID variantId,
        @Min(0) Integer availableQuantity,
        @Min(0) Integer incomingQuantity,
        @Min(1) Integer leadTimeDays,
        @DecimalMin("0.5") Double serviceLevel,
        @Min(1) Integer targetCoverDays,
        @Min(1) Integer minimumOrderQuantity,
        @Min(1) Integer packSize,
        @Min(1) Integer forecastHorizonDays) {
}
