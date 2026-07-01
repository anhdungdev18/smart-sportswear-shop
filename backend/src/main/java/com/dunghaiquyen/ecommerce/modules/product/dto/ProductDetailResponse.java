package com.dunghaiquyen.ecommerce.modules.product.dto;

import com.dunghaiquyen.ecommerce.modules.product.entity.Gender;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductStatus;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductType;
import java.util.List;
import java.util.UUID;

/**
 * reviewSummary/relatedProducts (Phase N4) are only populated on the PUBLIC
 * detail path (ProductService.getPublicDetail) - null/empty on the admin
 * path, since neither is meaningful there and computing them would just be
 * extra queries the admin screen does not need. See ProductService.assembleDetail.
 */
public record ProductDetailResponse(
        UUID id,
        String name,
        String slug,
        String shortDescription,
        String description,
        Gender gender,
        String sportType,
        ProductType productType,
        CatalogRefResponse brand,
        CatalogRefResponse category,
        List<ProductImageResponse> images,
        List<ProductVariantResponse> variants,
        ProductStatus status,
        boolean isFeatured,
        ReviewSummaryResponse reviewSummary,
        List<ProductListItemResponse> relatedProducts) {
}
