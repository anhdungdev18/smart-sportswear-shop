package com.dunghaiquyen.ecommerce.modules.collection.dto;

import com.dunghaiquyen.ecommerce.common.validation.NullOrNotBlank;
import com.dunghaiquyen.ecommerce.common.validation.Patterns;
import com.dunghaiquyen.ecommerce.modules.collection.entity.CollectionStatus;
import com.dunghaiquyen.ecommerce.modules.collection.entity.CollectionType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/** PATCH semantics: null means "leave unchanged". Use clearBrand=true to remove the brand tag. */
public record CollectionUpdateRequest(

        @NullOrNotBlank(message = "Name must not be blank")
        @Size(max = 200, message = "Name must be at most 200 characters")
        String name,

        @Size(max = 220, message = "Slug must be at most 220 characters")
        @Pattern(regexp = Patterns.SLUG, message = "Slug must be lowercase letters, numbers and hyphens only")
        String slug,

        String description,

        @Size(max = 500, message = "Short description must be at most 500 characters")
        String shortDescription,

        CollectionType collectionType,

        UUID brandId,

        Boolean clearBrand,

        @Size(max = 50, message = "Season must be at most 50 characters")
        String season,

        Integer year,

        @Size(max = 500)
        String bannerImageUrl,

        @Size(max = 500)
        String coverImageUrl,

        CollectionStatus status,

        Instant startsAt,

        Instant endsAt,

        Integer sortOrder,

        Boolean isFeatured) {
}
