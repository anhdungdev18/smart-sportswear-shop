package com.dunghaiquyen.ecommerce.modules.product.dto;

import com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * stockQuantity here is the INITIAL stock of a brand-new variant, set directly
 * (no inventory_transactions row) - it establishes a starting state, it does
 * not mutate existing stock. Compare with VariantUpdateRequest, which has no
 * stock field at all: once a variant exists, stock changes must go through
 * the inventory module (reserve/confirm/release/import/export/adjust), never
 * through this product-catalog endpoint.
 */
public record VariantCreateRequest(

        @NotBlank(message = "SKU is required")
        @Size(max = 100, message = "SKU must be at most 100 characters")
        String sku,

        @NotBlank(message = "Size is required")
        @Size(max = 50, message = "Size must be at most 50 characters")
        String size,

        @NotBlank(message = "Color is required")
        @Size(max = 50, message = "Color must be at most 50 characters")
        String color,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,

        @DecimalMin(value = "0.0", inclusive = false, message = "Compare-at price must be greater than 0")
        BigDecimal compareAtPrice,

        @PositiveOrZero(message = "Stock quantity must be 0 or more")
        Integer stockQuantity,

        VariantStatus status) {
}
