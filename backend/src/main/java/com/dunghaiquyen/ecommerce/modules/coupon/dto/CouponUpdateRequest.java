package com.dunghaiquyen.ecommerce.modules.coupon.dto;

import com.dunghaiquyen.ecommerce.modules.coupon.entity.CouponStatus;
import jakarta.validation.constraints.Positive;
import java.time.Instant;

/**
 * PATCH semantics: every field optional, null means "leave unchanged". code
 * and promotionId are deliberately not editable: this coupon may already
 * have recorded usages whose discount_amount was computed from the
 * promotion's shape at the time - changing either after the fact would make
 * past usages inconsistent with the coupon's current meaning.
 */
public record CouponUpdateRequest(

        String description,

        CouponStatus status,

        Instant startsAt,

        Instant endsAt,

        @Positive(message = "Usage limit must be greater than 0")
        Integer usageLimit,

        @Positive(message = "Per-user limit must be greater than 0")
        Integer perUserLimit) {
}
