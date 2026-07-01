package com.dunghaiquyen.ecommerce.modules.product.dto;

import com.dunghaiquyen.ecommerce.modules.product.entity.ProductStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record AdminProductListItemResponse(
        UUID id,
        String name,
        String slug,
        ProductStatus status,
        boolean isFeatured,
        CatalogRefResponse brand,
        CatalogRefResponse category,
        String thumbnail,
        BigDecimal minPrice,
        BigDecimal maxPrice) {
}
