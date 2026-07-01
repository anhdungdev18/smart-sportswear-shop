package com.dunghaiquyen.ecommerce.modules.collection.dto;

import com.dunghaiquyen.ecommerce.modules.collection.entity.CollectionType;
import com.dunghaiquyen.ecommerce.modules.product.dto.ProductListItemResponse;
import java.util.List;
import java.util.UUID;

/** Public collection detail — same as listing shape plus the products belonging to it. */
public record PublicCollectionDetailResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String shortDescription,
        CollectionType collectionType,
        String season,
        Integer year,
        String bannerImageUrl,
        String coverImageUrl,
        int sortOrder,
        boolean isFeatured,
        List<ProductListItemResponse> products) {
}
