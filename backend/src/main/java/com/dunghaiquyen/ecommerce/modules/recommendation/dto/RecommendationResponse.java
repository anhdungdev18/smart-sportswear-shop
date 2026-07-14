package com.dunghaiquyen.ecommerce.modules.recommendation.dto;

import java.util.List;
import java.util.UUID;

public record RecommendationResponse(
        UUID sourceProductId,
        String type,
        List<RecommendationItemResponse> items
) {
}