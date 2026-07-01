package com.dunghaiquyen.ecommerce.modules.review.dto;

/** Public review list query (GET /api/v1/products/{productId}/reviews): page, limit only - always APPROVED. */
public record ReviewListQuery(Integer page, Integer limit) {
}
