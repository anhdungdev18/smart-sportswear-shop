package com.dunghaiquyen.ecommerce.modules.inventory.dto;

import com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus;
import java.util.UUID;

/** Query params per API_SPEC_PHASE1.md 11.1 - every field optional. */
public record InventoryListQuery(
        Integer page,
        Integer limit,
        String keyword,
        UUID productId,
        UUID categoryId,
        UUID brandId,
        VariantStatus status) {
}
