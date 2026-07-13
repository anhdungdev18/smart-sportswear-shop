package com.dunghaiquyen.ecommerce.modules.recommendation.dto;

import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationAlgorithm;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationEventType;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationSourceType;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record RecommendationLogRequest(
        @NotNull RecommendationEventType eventType,
        @NotNull RecommendationSourceType sourceType,
        RecommendationType recommendationType,

        UUID sourceProductId,
        List<UUID> sourceProductIds,
        UUID cartId,

        @NotNull UUID recommendedProductId,

        @Min(1) Integer position,

        RecommendationAlgorithm algorithm,

        Double support,
        Double confidence,
        Double lift,
        Long pairCount,

        @Size(max = 255) String reason
) {
}