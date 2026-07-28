package com.dunghaiquyen.ecommerce.modules.recommendation.repository;

import java.util.UUID;

public interface RecommendationLogProductStatsProjection {

    UUID getProductId();

    String getProductName();

    Long getImpressions();

    Long getClicks();

    Long getAddToCarts();
}