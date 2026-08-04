package com.dunghaiquyen.ecommerce.modules.product.dto;

import com.dunghaiquyen.ecommerce.modules.product.entity.ProductStatus;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductType;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductListItemResponse(
        UUID id,
        String name,
        String slug,
        String shortDescription,
        CatalogRefResponse brand,
        CatalogRefResponse category,
        String thumbnail,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal salePrice,
        BigDecimal compareAtPrice,
        Integer discountPercent,
        int availableQuantity,
        ProductStatus status,
        ProductType productType) {
}
