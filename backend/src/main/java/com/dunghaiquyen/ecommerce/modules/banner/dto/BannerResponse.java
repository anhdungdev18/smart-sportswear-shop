package com.dunghaiquyen.ecommerce.modules.banner.dto;

import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerPlacement;
import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BannerResponse(
        UUID id,
        String name,
        String code,
        BannerPlacement placement,
        BannerStatus status,
        Instant startsAt,
        Instant endsAt,
        List<BannerItemResponse> items,
        Instant createdAt,
        Instant updatedAt) {
}
