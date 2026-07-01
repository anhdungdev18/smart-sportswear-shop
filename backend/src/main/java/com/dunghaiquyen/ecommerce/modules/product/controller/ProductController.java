package com.dunghaiquyen.ecommerce.modules.product.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.product.dto.ProductDetailResponse;
import com.dunghaiquyen.ecommerce.modules.product.dto.ProductListItemResponse;
import com.dunghaiquyen.ecommerce.modules.product.dto.ProductListQuery;
import com.dunghaiquyen.ecommerce.modules.product.dto.ProductSuggestionResponse;
import com.dunghaiquyen.ecommerce.modules.product.service.ProductService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public, read-only catalog browsing (API_SPEC_PHASE1.md 5.3/5.4) - only
 * ACTIVE products are visible. "/search-suggestions" is a literal path
 * segment, so Spring resolves it ahead of the "/{slugOrId}" pattern
 * regardless of declaration order - no separate route-ordering concern here.
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<List<ProductListItemResponse>> list(@ModelAttribute ProductListQuery query) {
        ProductService.ListResult result = productService.listPublic(query);
        return ApiResponse.ok(result.items(), result.meta());
    }

    /** Phase N3 autocomplete - GET /api/v1/products/search-suggestions?q=... */
    @GetMapping("/search-suggestions")
    public ApiResponse<List<ProductSuggestionResponse>> searchSuggestions(
            @RequestParam(required = false) String q) {
        return ApiResponse.ok(productService.searchSuggestions(q));
    }

    @GetMapping("/{slugOrId}")
    public ApiResponse<ProductDetailResponse> detail(@PathVariable String slugOrId) {
        return ApiResponse.ok(productService.getPublicDetail(slugOrId));
    }
}
