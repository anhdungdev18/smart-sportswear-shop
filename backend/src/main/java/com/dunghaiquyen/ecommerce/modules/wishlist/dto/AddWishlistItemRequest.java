package com.dunghaiquyen.ecommerce.modules.wishlist.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddWishlistItemRequest(

        @NotNull(message = "Product is required")
        UUID productId) {
}
