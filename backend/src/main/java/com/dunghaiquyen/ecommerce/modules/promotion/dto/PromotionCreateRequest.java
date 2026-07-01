package com.dunghaiquyen.ecommerce.modules.promotion.dto;

import com.dunghaiquyen.ecommerce.common.validation.Patterns;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionScope;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionStatus;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Cross-field rules (which of discountPercent/discountAmount is required for
 * a given type, productIds required iff scope is PRODUCT) are validated in
 * PromotionService, not here - they depend on more than one field at once,
 * which bean validation handles awkwardly compared to a single service check.
 */
public record PromotionCreateRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 200, message = "Name must be at most 200 characters")
        String name,

        @NotBlank(message = "Slug is required")
        @Size(max = 220, message = "Slug must be at most 220 characters")
        @Pattern(regexp = Patterns.SLUG, message = "Slug must be lowercase letters, numbers and hyphens only")
        String slug,

        String description,

        @NotNull(message = "Type is required")
        PromotionType type,

        @NotNull(message = "Scope is required")
        PromotionScope scope,

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
