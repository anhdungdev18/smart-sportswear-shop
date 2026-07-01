package com.dunghaiquyen.ecommerce.modules.collection.dto;

import com.dunghaiquyen.ecommerce.modules.collection.entity.CollectionType;
import java.util.UUID;

/**
 * Public listing shape — no status, no internal timestamps. The listing
 * endpoint only ever returns ACTIVE + within-time-window collections, so
 * exposing status here would add noise without adding information.
 */
public record PublicCollectionResponse(
        UUID id,
        String name,
        String slug,
        String shortDescription,
        CollectionType collectionType,
        String season,
        Integer year,
        String bannerImageUrl,
        String coverImageUrl,
        int sortOrder,
        boolean isFeatured) {
}
