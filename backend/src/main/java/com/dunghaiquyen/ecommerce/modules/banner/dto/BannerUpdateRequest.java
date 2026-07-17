package com.dunghaiquyen.ecommerce.modules.banner.dto;

import com.dunghaiquyen.ecommerce.common.validation.NullOrNotBlank;
import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerPlacement;
import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerStatus;
import java.time.Instant;

/** Partial update (null = leave unchanged). */
public record BannerUpdateRequest(
        @NullOrNotBlank String name,
        @NullOrNotBlank String code,
        BannerPlacement placement,
        BannerStatus status,
        Instant startsAt,
        Instant endsAt) {
}
