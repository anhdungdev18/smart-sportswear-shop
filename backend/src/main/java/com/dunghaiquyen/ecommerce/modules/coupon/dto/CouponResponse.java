package com.dunghaiquyen.ecommerce.modules.coupon.dto;

import com.dunghaiquyen.ecommerce.modules.coupon.entity.CouponStatus;
import java.time.Instant;
import java.util.UUID;

public record CouponResponse(
        UUID id,
        UUID promotionId,
        String code,
        String description,
        CouponStatus status,
        Instant startsAt,
        Instant endsAt,
        Integer usageLimit,
        int usageCount,
        Integer perUserLimit,
        Instant createdAt,
        Instant updatedAt) {
}
