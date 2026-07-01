package com.dunghaiquyen.ecommerce.modules.product.dto;

import java.util.UUID;

/**
 * Separate from ProductImageResponse on purpose: width/height only ever
 * exist right after an upload (Cloudinary returns them in the same call) -
 * they are not persisted columns on product_images, so no other endpoint
 * (GET product detail, admin list, etc.) could ever populate them anyway.
 */
public record ProductImageUploadResponse(
        UUID id,
        String imageUrl,
        String publicId,
        String altText,
        boolean isPrimary,
        int sortOrder,
        Integer width,
        Integer height) {
}
