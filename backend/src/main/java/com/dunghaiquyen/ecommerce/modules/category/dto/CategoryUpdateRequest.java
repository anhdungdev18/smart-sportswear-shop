package com.dunghaiquyen.ecommerce.modules.category.dto;

import com.dunghaiquyen.ecommerce.common.validation.Patterns;
import com.dunghaiquyen.ecommerce.modules.category.entity.CategoryStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** PATCH semantics: every field optional, null means "leave unchanged". */
public record CategoryUpdateRequest(

        @Size(max = 150, message = "Name must be at most 150 characters")
        String name,

        @Size(max = 180, message = "Slug must be at most 180 characters")
        @Pattern(regexp = Patterns.SLUG, message = "Slug must be lowercase letters, numbers and hyphens only")
        String slug,

        String description,

        CategoryStatus status) {
}
