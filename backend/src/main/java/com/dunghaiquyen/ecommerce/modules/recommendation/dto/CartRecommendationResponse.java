package com.dunghaiquyen.ecommerce.modules.recommendation.dto;

import java.util.List;
import java.util.UUID;

public record CartRecommendationResponse(
        UUID cartId,
        List<UUID> sourceProductIds,
        String type,
        List<RecommendationItemResponse> items
) {
}