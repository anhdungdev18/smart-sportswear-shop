package com.dunghaiquyen.ecommerce.visualsearch.api;

import com.dunghaiquyen.ecommerce.modules.product.dto.ProductListItemResponse;
import java.util.UUID;

public record VisualSearchResult(
        ProductListItemResponse product,
        UUID matchedImageId,
        String matchedImageUrl,
        double similarity) {
}
