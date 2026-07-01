package com.dunghaiquyen.ecommerce.modules.review.dto;

import com.dunghaiquyen.ecommerce.modules.review.entity.ReviewStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateReviewStatusRequest(

        @NotNull(message = "Status is required")
        ReviewStatus status) {
}
