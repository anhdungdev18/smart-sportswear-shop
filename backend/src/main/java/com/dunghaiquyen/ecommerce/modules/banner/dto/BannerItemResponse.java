package com.dunghaiquyen.ecommerce.modules.banner.dto;

import java.util.UUID;

public record BannerItemResponse(
        UUID id,
        UUID bannerId,
        String title,
        String subtitle,
        String imageUrl,
        String targetUrl,
        UUID productId,
        int sortOrder,
        boolean isActive) {
}
