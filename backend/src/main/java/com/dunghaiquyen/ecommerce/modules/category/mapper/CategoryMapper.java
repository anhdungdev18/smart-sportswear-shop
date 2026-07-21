package com.dunghaiquyen.ecommerce.modules.category.mapper;

import com.dunghaiquyen.ecommerce.modules.category.dto.CategoryResponse;
import com.dunghaiquyen.ecommerce.modules.category.dto.CategoryTreeResponse;
import com.dunghaiquyen.ecommerce.modules.category.entity.Category;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getStatus(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getParent() != null ? category.getParent().getName() : null,
                category.getParent() != null ? category.getParent().getSlug() : null,
                category.getNodeType(),
                category.getSortOrder());
    }

    public CategoryTreeResponse toTreeResponse(Category category, List<CategoryTreeResponse> children) {
        return new CategoryTreeResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getStatus(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getParent() != null ? category.getParent().getName() : null,
                category.getParent() != null ? category.getParent().getSlug() : null,
                category.getNodeType(),
                category.getSortOrder(),
                children);
    }
}
