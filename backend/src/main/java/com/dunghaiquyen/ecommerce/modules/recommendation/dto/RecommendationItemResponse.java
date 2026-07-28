package com.dunghaiquyen.ecommerce.modules.recommendation.dto;

import com.dunghaiquyen.ecommerce.modules.product.dto.ProductListItemResponse;

public record RecommendationItemResponse(
        ProductListItemResponse product,
        double support,
        double confidence,
        double lift,
        long pairCount,
        String reason
) {
}