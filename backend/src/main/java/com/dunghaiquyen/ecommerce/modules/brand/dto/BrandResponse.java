package com.dunghaiquyen.ecommerce.modules.brand.dto;

import com.dunghaiquyen.ecommerce.modules.brand.entity.BrandStatus;
import java.util.UUID;

public record BrandResponse(
        UUID id,
        String name,
        String slug,
        String description,
        BrandStatus status) {
}
