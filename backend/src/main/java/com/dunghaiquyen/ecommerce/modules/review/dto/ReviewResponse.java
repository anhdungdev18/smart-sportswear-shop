package com.dunghaiquyen.ecommerce.modules.review.dto;

import com.dunghaiquyen.ecommerce.modules.review.entity.ReviewStatus;
import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID productId,
        UUID userId,
        String reviewerName,
        int rating,
        String title,
        String content,
        ReviewStatus status,
        boolean verifiedPurchase,
        Instant createdAt) {
}
