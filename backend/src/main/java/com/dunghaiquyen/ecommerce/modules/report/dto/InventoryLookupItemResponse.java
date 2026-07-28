package com.dunghaiquyen.ecommerce.modules.report.dto;

import com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus;
import java.time.Instant;
import java.util.UUID;

public record InventoryLookupItemResponse(
        UUID variantId,
        UUID productId,
        String productName,
        String slug,
        String sku,
        String size,
        String color,
        int stockQuantity,
        int reservedQuantity,
        int availableQuantity,
        VariantStatus status,
        Instant updatedAt
) {
}
