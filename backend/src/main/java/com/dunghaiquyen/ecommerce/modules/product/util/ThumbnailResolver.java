package com.dunghaiquyen.ecommerce.modules.product.util;

import com.dunghaiquyen.ecommerce.modules.product.entity.ProductImage;
import java.util.Comparator;
import java.util.List;

/**
 * Shared by catalog listing and cart response assembly: both need "the one
 * image to show for this product" using the same rule (primary image, else
 * lowest sort order, else none).
 */
public final class ThumbnailResolver {

    private ThumbnailResolver() {
    }

    public static String resolve(List<ProductImage> images) {
        return images.stream()
                .filter(ProductImage::isPrimary)
                .findFirst()
                .or(() -> images.stream().min(Comparator.comparingInt(ProductImage::getSortOrder)))
                .map(ProductImage::getImageUrl)
                .orElse(null);
    }
}
