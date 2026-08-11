package com.dunghaiquyen.ecommerce.modules.promotion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Admin creates a product-percentage promotion with a time window. */
public record PromotionCreateRequest(
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin("0.01") @DecimalMax("100.00") BigDecimal discountPercent,
        Instant startsAt,
        Instant endsAt,
        @NotEmpty List<UUID> productIds) {
}
