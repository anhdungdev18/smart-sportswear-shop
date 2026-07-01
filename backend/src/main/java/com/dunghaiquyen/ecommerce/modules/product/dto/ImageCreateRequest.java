package com.dunghaiquyen.ecommerce.modules.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ImageCreateRequest(

        @NotBlank(message = "Image URL is required")
        @Size(max = 500, message = "Image URL must be at most 500 characters")
        String imageUrl,

        @Size(max = 255, message = "Public id must be at most 255 characters")
        String publicId,

        @Size(max = 255, message = "Alt text must be at most 255 characters")
        String altText,

        @PositiveOrZero(message = "Sort order must be 0 or more")
        Integer sortOrder,

        Boolean isPrimary) {
}
