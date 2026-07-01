package com.dunghaiquyen.ecommerce.modules.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record ExportStockRequest(
        @NotNull(message = "Variant is required") UUID variantId,
        @Positive(message = "Quantity must be greater than 0") int quantity,
        String note) {
}
