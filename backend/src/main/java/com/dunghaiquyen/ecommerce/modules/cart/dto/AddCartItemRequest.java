package com.dunghaiquyen.ecommerce.modules.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record AddCartItemRequest(

        @NotNull(message = "Variant is required")
        UUID variantId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        Integer quantity) {
}
