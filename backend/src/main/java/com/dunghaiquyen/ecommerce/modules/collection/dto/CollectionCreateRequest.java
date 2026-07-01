package com.dunghaiquyen.ecommerce.modules.collection.dto;

import com.dunghaiquyen.ecommerce.common.validation.Patterns;
import com.dunghaiquyen.ecommerce.modules.collection.entity.CollectionStatus;
import com.dunghaiquyen.ecommerce.modules.collection.entity.CollectionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CollectionCreateRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 200, message = "Name must be at most 200 characters")
        String name,

        @NotBlank(message = "Slug is required")
        @Size(max = 220, message = "Slug must be at most 220 characters")
        @Pattern(regexp = Patterns.SLUG, message = "Slug must be lowercase letters, numbers and hyphens only")
        String slug,

        String description,

        @Size(max = 500, message = "Short description must be at most 500 characters")
        String shortDescription,

        @NotNull(message = "Collection type is required")
        CollectionType collectionType,

        @Size(max = 50, message = "Season must be at most 50 characters")
        String season,

        Integer year,

        @Size(max = 500, message = "Banner image URL must be at most 500 characters")
        String bannerImageUrl,

        @Size(max = 500, message = "Cover image URL must be at most 500 characters")
        String coverImageUrl,

        CollectionStatus status,

        Instant startsAt,

        Instant endsAt,

        Integer sortOrder,

        Boolean isFeatured) {
}
