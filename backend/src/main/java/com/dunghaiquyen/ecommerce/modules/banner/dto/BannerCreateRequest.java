package com.dunghaiquyen.ecommerce.modules.banner.dto;

import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerPlacement;
import com.dunghaiquyen.ecommerce.modules.banner.entity.BannerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record BannerCreateRequest(
        @NotBlank String name,
        @NotBlank String code,
        @NotNull BannerPlacement placement,
        BannerStatus status,
        Instant startsAt,
        Instant endsAt) {
}
