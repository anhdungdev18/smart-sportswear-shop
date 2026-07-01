package com.dunghaiquyen.ecommerce.modules.product.dto;

/** Phase N4 PDP completion - APPROVED reviews only (never pending/rejected). */
public record ReviewSummaryResponse(double averageRating, long reviewCount) {
}
