package com.dunghaiquyen.ecommerce.modules.category.dto;

import com.dunghaiquyen.ecommerce.modules.category.entity.CategoryNodeType;
import com.dunghaiquyen.ecommerce.modules.category.entity.CategoryStatus;
import java.util.List;
import java.util.UUID;

public record CategoryTreeResponse(
        UUID id,
        String name,
        String slug,
        String description,
        CategoryStatus status,
        UUID parentId,
        String parentName,
        String parentSlug,
        CategoryNodeType nodeType,
        int sortOrder,
        List<CategoryTreeResponse> children) {
}
