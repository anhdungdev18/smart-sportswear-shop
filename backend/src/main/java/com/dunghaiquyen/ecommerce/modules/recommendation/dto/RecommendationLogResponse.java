package com.dunghaiquyen.ecommerce.modules.recommendation.dto;

import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationEventType;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationSourceType;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationType;
import java.time.Instant;
import java.util.UUID;

public record RecommendationLogResponse(
        UUID id,
        RecommendationEventType eventType,
        RecommendationSourceType sourceType,
        RecommendationType recommendationType,
        UUID recommendedProductId,
        Instant createdAt
) {
}