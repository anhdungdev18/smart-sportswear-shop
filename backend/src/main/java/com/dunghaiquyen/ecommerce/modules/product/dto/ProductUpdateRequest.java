package com.dunghaiquyen.ecommerce.modules.product.dto;

import com.dunghaiquyen.ecommerce.common.validation.NullOrNotBlank;
import com.dunghaiquyen.ecommerce.common.validation.Patterns;
import com.dunghaiquyen.ecommerce.modules.product.entity.Gender;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * PATCH semantics: every field optional, null means "leave unchanged". Note
 * slug needs no separate blank guard: Patterns.SLUG already requires at least
 * one [a-z0-9] character, so "" or "   " already fail @Pattern.
 */
public record ProductUpdateRequest(

        @NullOrNotBlank(message = "Name must not be blank")
        @Size(max = 255, message = "Name must be at most 255 characters")
        String name,

        @Size(max = 300, message = "Slug must be at most 300 characters")
        @Pattern(regexp = Patterns.SLUG, message = "Slug must be lowercase letters, numbers and hyphens only")
        String slug,

        @Size(max = 500, message = "Short description must be at most 500 characters")
        String shortDescription,

        String description,

        UUID categoryId,

        UUID brandId,

        Gender gender,

        @Size(max = 50, message = "Sport type must be at most 50 characters")
        String sportType,

        /** Catalog V2 (V10): null means "leave unchanged", matching the PATCH convention for this record. */
        com.dunghaiquyen.ecommerce.modules.product.entity.ProductType productType,

        ProductStatus status,

        Boolean isFeatured) {
}
