package com.dunghaiquyen.ecommerce.modules.inventory.dto;

import com.dunghaiquyen.ecommerce.modules.inventory.entity.InventoryTransactionType;
import java.time.LocalDate;
import java.util.UUID;

/** Query params per API_SPEC_PHASE1.md 11.5. */
public record InventoryTransactionListQuery(
        Integer page, Integer limit, UUID variantId, InventoryTransactionType type, LocalDate dateFrom, LocalDate dateTo) {
}
