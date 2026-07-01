package com.dunghaiquyen.ecommerce.modules.product.dto;

import com.dunghaiquyen.ecommerce.common.validation.NullOrNotBlank;
import com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * PATCH semantics: every field optional, null means "leave unchanged".
 * Deliberately has no stockQuantity/reservedQuantity field - see
 * VariantCreateRequest javadoc. Stock changes after creation are the
 * inventory module's job, not catalog's.
 */
public record VariantUpdateRequest(

        @NullOrNotBlank(message = "Size must not be blank")
        @Size(max = 50, message = "Size must be at most 50 characters")
        String size,

        @NullOrNotBlank(message = "Color must not be blank")
        @Size(max = 50, message = "Color must be at most 50 characters")
        String color,

        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,

        @DecimalMin(value = "0.0", inclusive = false, message = "Compare-at price must be greater than 0")
        BigDecimal compareAtPrice,

        VariantStatus status) {
}
