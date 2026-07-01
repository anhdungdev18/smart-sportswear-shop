package com.dunghaiquyen.ecommerce.modules.product.dto;

import com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * availableQuantity = stockQuantity - reservedQuantity (ERD_PHASE1.md 6.8) is
 * exposed instead of the two raw columns: callers never need reservedQuantity
 * directly, and showing it invites someone to "fix" it via this same response
 * shape later, which is exactly the write path InventoryService must own alone.
 */
public record ProductVariantResponse(
        UUID id,
        String sku,
        String size,
        String color,
        BigDecimal price,
        BigDecimal compareAtPrice,
        int availableQuantity,
        VariantStatus status) {
}
