package com.dunghaiquyen.ecommerce.modules.recommendation.dto;

import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationEventType;
import com.dunghaiquyen.ecommerce.modules.recommendation.entity.RecommendationSourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RecommendationLogRequest(
        @NotNull RecommendationEventType eventType,
        @NotNull RecommendationSourceType sourceType,
        UUID sourceProductId,
        UUID cartId,
        @NotNull UUID recommendedProductId,
        @Min(1) Integer position
) {
}
