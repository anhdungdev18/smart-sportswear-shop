package com.dunghaiquyen.ecommerce.modules.category.dto;

import com.dunghaiquyen.ecommerce.common.validation.NullOrNotBlank;
import com.dunghaiquyen.ecommerce.common.validation.Patterns;
import com.dunghaiquyen.ecommerce.modules.category.entity.CategoryNodeType;
import com.dunghaiquyen.ecommerce.modules.category.entity.CategoryStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * PATCH semantics: every field optional, null means "leave unchanged". Note
 * slug needs no separate blank guard: Patterns.SLUG already requires at least
 * one [a-z0-9] character, so "" or "   " already fail @Pattern.
 */
public record CategoryUpdateRequest(

        @NullOrNotBlank(message = "Name must not be blank")
        @Size(max = 150, message = "Name must be at most 150 characters")
        String name,

        @Size(max = 180, message = "Slug must be at most 180 characters")
        @Pattern(regexp = Patterns.SLUG, message = "Slug must be lowercase letters, numbers and hyphens only")
        String slug,

        String description,

        CategoryStatus status,

        UUID parentId,

        Boolean clearParent,

        CategoryNodeType nodeType,

        Integer sortOrder) {
}
