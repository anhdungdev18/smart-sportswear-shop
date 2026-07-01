package com.dunghaiquyen.ecommerce.modules.promotion.dto;

import com.dunghaiquyen.ecommerce.common.validation.NullOrNotBlank;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * PATCH semantics: every field optional, null means "leave unchanged" -
 * except productIds, where a non-null (possibly empty) list REPLACES the
 * current product set wholesale (simpler and less error-prone than an
 * add/remove delta API for this phase). type and scope are deliberately not
 * editable here: changing either after a promotion may already be in use by
 * a coupon would silently change that coupon's discount semantics - if that
 * is ever needed, the safer path is creating a new promotion.
 */
public record PromotionUpdateRequest(

        @NullOrNotBlank(message = "Name must not be blank")
        @Size(max = 200, message = "Name must be at most 200 characters")
        String name,

        String description,

        PromotionStatus status,

        @DecimalMin(value = "0.0", inclusive = false, message = "Discount percent must be greater than 0")
        BigDecimal discountPercent,

        @DecimalMin(value = "0.0", inclusive = false, message = "Discount amount must be greater than 0")
        BigDecimal discountAmount,

        @DecimalMin(value = "0.0", message = "Minimum order amount must be 0 or more")
        BigDecimal minOrderAmount,

        @DecimalMin(value = "0.0", inclusive = false, message = "Max discount amount must be greater than 0")
        BigDecimal maxDiscountAmount,

        Instant startsAt,

        Instant endsAt,

        @Positive(message = "Usage limit must be greater than 0")
        Integer usageLimit,

        List<UUID> productIds) {
}
