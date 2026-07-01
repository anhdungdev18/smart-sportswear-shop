package com.dunghaiquyen.ecommerce.modules.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Not a "leave unchanged if null" PATCH like the catalog DTOs - quantity is
 * the only field this resource has, so omitting it would mean "patch
 * nothing". Per API_SPEC_PHASE1.md 6.3 and the explicit decision to reject
 * non-positive quantities with a clear 422 rather than silently treating
 * "quantity <= 0" as a delete.
 */
public record UpdateCartItemRequest(

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        Integer quantity) {
}
