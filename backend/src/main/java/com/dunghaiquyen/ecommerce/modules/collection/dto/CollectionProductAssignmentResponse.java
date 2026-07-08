package com.dunghaiquyen.ecommerce.modules.collection.dto;

import java.util.UUID;

public record CollectionProductAssignmentResponse(
        UUID id,
        String name,
        String slug,
        String status,
        String thumbnail,
        String brandName,
        String categoryName) {
}
