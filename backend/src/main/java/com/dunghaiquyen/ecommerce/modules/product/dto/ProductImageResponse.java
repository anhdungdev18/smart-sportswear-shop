package com.dunghaiquyen.ecommerce.modules.product.dto;

import java.util.UUID;

public record ProductImageResponse(
        UUID id,
        String imageUrl,
        String publicId,
        String altText,
        boolean isPrimary,
        int sortOrder) {
}
