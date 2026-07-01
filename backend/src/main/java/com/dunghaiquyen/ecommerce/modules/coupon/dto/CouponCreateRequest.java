package com.dunghaiquyen.ecommerce.modules.coupon.dto;

import com.dunghaiquyen.ecommerce.modules.coupon.entity.CouponStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/**
 * promotionId is required (not nullable like the DB column) - see Coupon's
 * class javadoc: a coupon with no promotion has no discount type/amount/scope
 * to apply, so it could never actually be used at checkout. code is
 * normalized to upper-case/trimmed by CouponService, not here, so the
 * uniqueness check and the stored value always agree.
 */
public record CouponCreateRequest(

        @NotBlank(message = "Code is required")
        @Size(max = 80, message = "Code must be at most 80 characters")
        String code,

        @NotNull(message = "Promotion is required")
        UUID promotionId,

        String description,

        CouponStatus status,

        Instant startsAt,

        Instant endsAt,

        @Positive(message = "Usage limit must be greater than 0")
        Integer usageLimit,

        @Positive(message = "Per-user limit must be greater than 0")
        Integer perUserLimit) {
}
