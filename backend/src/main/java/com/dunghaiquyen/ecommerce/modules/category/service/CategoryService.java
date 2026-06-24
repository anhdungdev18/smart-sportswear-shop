package com.dunghaiquyen.ecommerce.modules.category.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.modules.category.dto.CategoryCreateRequest;
import com.dunghaiquyen.ecommerce.modules.category.dto.CategoryResponse;
import com.dunghaiquyen.ecommerce.modules.category.dto.CategoryUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.category.entity.Category;
import com.dunghaiquyen.ecommerce.modules.category.entity.CategoryStatus;
import com.dunghaiquyen.ecommerce.modules.category.mapper.CategoryMapper;
import com.dunghaiquyen.ecommerce.modules.category.repository.CategoryRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listActive() {
        return categoryRepository.findAllByStatusOrderByNameAsc(CategoryStatus.ACTIVE).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryCreateRequest request) {
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new BusinessRuleException("Slug already exists: " + request.slug());
        }

        Category category = new Category();
        category.setName(request.name().trim());
        category.setSlug(request.slug());
        category.setDescription(request.description());
        category.setStatus(request.status() != null ? request.status() : CategoryStatus.ACTIVE);

        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("Slug already exists: " + request.slug());
        }
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (request.slug() != null && categoryRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new BusinessRuleException("Slug already exists: " + request.slug());
        }

        if (request.name() != null) {
            category.setName(request.name().trim());
        }
        if (request.slug() != null) {
            category.setSlug(request.slug());
        }
        if (request.description() != null) {
            category.setDescription(request.description());
        }
        if (request.status() != null) {
            category.setStatus(request.status());
        }

        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("Slug already exists: " + request.slug());
        }
        return categoryMapper.toResponse(category);
    }
}
