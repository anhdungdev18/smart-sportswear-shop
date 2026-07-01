package com.dunghaiquyen.ecommerce.modules.wishlist.dto;

import java.time.Instant;
import java.util.UUID;

public record WishlistItemResponse(
        UUID id, UUID productId, String productName, String thumbnail, Instant createdAt) {
}
