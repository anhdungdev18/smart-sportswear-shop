package com.dunghaiquyen.ecommerce.modules.collection.dto;

import com.dunghaiquyen.ecommerce.modules.collection.entity.CollectionStatus;
import com.dunghaiquyen.ecommerce.modules.collection.entity.CollectionType;
import com.dunghaiquyen.ecommerce.modules.product.dto.CatalogRefResponse;
import java.time.Instant;
import java.util.UUID;

/** Admin-facing full response (includes status, draft collections, timestamps). */
public record CollectionResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String shortDescription,
        CollectionType collectionType,
        CatalogRefResponse brand,
        String season,
        Integer year,
        String bannerImageUrl,
        String coverImageUrl,
        CollectionStatus status,
        Instant startsAt,
        Instant endsAt,
        int sortOrder,
        boolean isFeatured,
        Instant createdAt,
        Instant updatedAt) {
}
