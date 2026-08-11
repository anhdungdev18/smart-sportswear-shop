package com.dunghaiquyen.ecommerce.modules.promotion.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Public shape for the storefront flash-sale countdown. */
public record ActivePromotionResponse(
        UUID id,
        String name,
        String slug,
        BigDecimal discountPercent,
        Instant startsAt,
        Instant endsAt) {
}
