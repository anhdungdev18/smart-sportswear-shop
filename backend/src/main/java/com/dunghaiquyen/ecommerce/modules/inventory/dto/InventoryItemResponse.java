package com.dunghaiquyen.ecommerce.modules.inventory.dto;

import com.dunghaiquyen.ecommerce.modules.product.dto.CatalogRefResponse;
import com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * One row per variant - "tồn kho được quản lý tại variant" (PHASE1_SPEC.md 6.8).
 * availableQuantity = stockQuantity - reservedQuantity, the same invariant used
 * everywhere else stock is checked (cart, checkout, export/adjust-down here).
 */
public record InventoryItemResponse(
        UUID variantId,
        UUID productId,
        String productName,
        String thumbnail,
        String sku,
        String size,
        String color,
        CatalogRefResponse category,
        CatalogRefResponse brand,
        BigDecimal price,
        int stockQuantity,
        int reservedQuantity,
        int availableQuantity,
        VariantStatus status) {
}
