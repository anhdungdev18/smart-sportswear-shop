package com.dunghaiquyen.ecommerce.modules.product.dto;

import com.dunghaiquyen.ecommerce.modules.product.entity.ProductStatus;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductType;
import java.util.UUID;

/** Admin listing - unlike the public ProductListQuery, status is a filter (optional), never an implicit ACTIVE-only restriction. */
public record AdminProductListQuery(
        Integer page,
        Integer limit,
        String keyword,
        ProductStatus status,
        UUID categoryId,
        UUID brandId,
        Boolean featured,
        ProductType productType) {
}
