package com.dunghaiquyen.ecommerce.modules.brand.dto;

import com.dunghaiquyen.ecommerce.common.validation.NullOrNotBlank;
import com.dunghaiquyen.ecommerce.common.validation.Patterns;
import com.dunghaiquyen.ecommerce.modules.brand.entity.BrandStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * PATCH semantics: every field optional, null means "leave unchanged". Note
 * slug needs no separate blank guard: Patterns.SLUG already requires at least
 * one [a-z0-9] character, so "" or "   " already fail @Pattern.
 */
public record BrandUpdateRequest(

        @NullOrNotBlank(message = "Name must not be blank")
        @Size(max = 150, message = "Name must be at most 150 characters")
        String name,

        @Size(max = 180, message = "Slug must be at most 180 characters")
        @Pattern(regexp = Patterns.SLUG, message = "Slug must be lowercase letters, numbers and hyphens only")
        String slug,

        String description,

        BrandStatus status) {
}
