package com.dunghaiquyen.ecommerce.modules.promotion.dto;

import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PromotionResponse(
        UUID id,
        String name,
        String slug,
        String description,
        BigDecimal discountPercent,
        Instant startsAt,
        Instant endsAt,
        PromotionStatus status,
        boolean live,
        int productCount,
        List<UUID> productIds) {
}
