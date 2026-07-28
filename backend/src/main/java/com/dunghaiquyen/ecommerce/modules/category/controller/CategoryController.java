package com.dunghaiquyen.ecommerce.modules.category.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.category.dto.CategoryResponse;
import com.dunghaiquyen.ecommerce.modules.category.dto.CategoryTreeResponse;
import com.dunghaiquyen.ecommerce.modules.category.service.CategoryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public, read-only: only ACTIVE categories are visible (API_SPEC_PHASE1.md 5.1). */
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> list() {
        return ApiResponse.ok(categoryService.listActive());
    }

    @GetMapping("/tree")
    public ApiResponse<List<CategoryTreeResponse>> tree() {
        return ApiResponse.ok(categoryService.listActiveTree());
    }

    @GetMapping("/{slugOrId}")
    public ApiResponse<CategoryResponse> detail(@PathVariable String slugOrId) {
        return ApiResponse.ok(categoryService.getActiveDetail(slugOrId));
    }
}
