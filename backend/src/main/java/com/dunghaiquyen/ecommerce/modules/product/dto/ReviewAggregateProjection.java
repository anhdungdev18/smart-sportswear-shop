package com.dunghaiquyen.ecommerce.modules.product.dto;

/** Spring Data interface projection for the avg/count aggregate query in ProductReviewAggregateRepository. */
public interface ReviewAggregateProjection {

    Double getAvgRating();

    Long getReviewCount();
}
