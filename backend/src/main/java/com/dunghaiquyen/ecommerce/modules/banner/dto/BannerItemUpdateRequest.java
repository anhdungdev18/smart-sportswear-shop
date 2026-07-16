package com.dunghaiquyen.ecommerce.modules.banner.dto;

import com.dunghaiquyen.ecommerce.common.validation.NullOrNotBlank;
import java.util.UUID;

/** Partial update (null = leave unchanged). productId cannot be explicitly cleared back to null in this phase. */
public record BannerItemUpdateRequest(
        String title,
        String subtitle,
        @NullOrNotBlank String imageUrl,
        String targetUrl,
        UUID productId,
        Integer sortOrder,
        Boolean isActive) {
}
