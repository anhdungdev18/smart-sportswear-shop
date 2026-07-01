package com.dunghaiquyen.ecommerce.modules.promotion.dto;

import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionScope;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionStatus;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PromotionResponse(
        UUID id,
        String name,
        String slug,
        String description,
        PromotionType type,
        PromotionScope scope,
        PromotionStatus status,
        BigDecimal discountPercent,
        BigDecimal discountAmount,
        BigDecimal minOrderAmount,
        BigDecimal maxDiscountAmount,
        Instant startsAt,
        Instant endsAt,
        Integer usageLimit,
        int usageCount,
        List<UUID> productIds,
        Instant createdAt,
        Instant updatedAt) {
}
