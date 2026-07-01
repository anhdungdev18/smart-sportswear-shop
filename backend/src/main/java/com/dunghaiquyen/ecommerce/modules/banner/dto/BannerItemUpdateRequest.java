package com.dunghaiquyen.ecommerce.modules.banner.dto;

import com.dunghaiquyen.ecommerce.common.validation.NullOrNotBlank;
import java.util.UUID;

/** Partial update (null = leave unchanged). productId has no way to be explicitly cleared back to null this phase - same accepted limitation PromotionUpdateRequest's own startsAt/endsAt already has. */
public record BannerItemUpdateRequest(
        String title,
        String subtitle,
        @NullOrNotBlank String imageUrl,
        String targetUrl,
        UUID productId,
        Integer sortOrder,
        Boolean isActive) {
}
