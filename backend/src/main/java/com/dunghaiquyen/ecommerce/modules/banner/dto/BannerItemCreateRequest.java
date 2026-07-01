package com.dunghaiquyen.ecommerce.modules.banner.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record BannerItemCreateRequest(
        String title,
        String subtitle,
        @NotBlank String imageUrl,
        String targetUrl,
        UUID productId,
        Integer sortOrder,
        Boolean isActive) {
}
