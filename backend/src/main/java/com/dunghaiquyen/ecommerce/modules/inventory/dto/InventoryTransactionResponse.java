package com.dunghaiquyen.ecommerce.modules.inventory.dto;

import com.dunghaiquyen.ecommerce.modules.inventory.entity.InventoryTransactionType;
import java.time.Instant;
import java.util.UUID;

public record InventoryTransactionResponse(
        UUID id,
        UUID variantId,
        String sku,
        UUID orderId,
        InventoryTransactionType type,
        int quantity,
        int beforeStockQuantity,
        int afterStockQuantity,
        int beforeReservedQuantity,
        int afterReservedQuantity,
        String note,
        UUID createdById,
        String createdByName,
        Instant createdAt) {
}
