package com.dunghaiquyen.ecommerce.modules.product.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.product.dto.ProductDetailResponse;
import com.dunghaiquyen.ecommerce.modules.product.dto.ProductListItemResponse;
import com.dunghaiquyen.ecommerce.modules.product.dto.ProductListQuery;
import com.dunghaiquyen.ecommerce.modules.product.dto.ProductSuggestionResponse;
import com.dunghaiquyen.ecommerce.modules.product.service.ProductService;
import com.dunghaiquyen.ecommerce.modules.product.entity.Gender;
import com.dunghaiquyen.ecommerce.visualsearch.api.VisualSearchRateLimiter;
import com.dunghaiquyen.ecommerce.visualsearch.api.VisualSearchResult;
import com.dunghaiquyen.ecommerce.visualsearch.api.VisualSearchService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

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
    private final VisualSearchService visualSearchService;
    private final VisualSearchRateLimiter visualSearchRateLimiter;

    public ProductController(
            ProductService productService,
            VisualSearchService visualSearchService,
            VisualSearchRateLimiter visualSearchRateLimiter) {
        this.productService = productService;
        this.visualSearchService = visualSearchService;
        this.visualSearchRateLimiter = visualSearchRateLimiter;
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

    @PostMapping(value = "/search-by-image", consumes = "multipart/form-data")
    public ApiResponse<List<VisualSearchResult>> searchByImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            HttpServletRequest request) {
        int boundedLimit = Math.max(1, Math.min(limit, 20));
        visualSearchRateLimiter.check(request.getRemoteAddr());
        return ApiResponse.ok(visualSearchService.search(
                image, boundedLimit, categoryId, gender, minPrice, maxPrice));
    }

    @GetMapping("/{slugOrId}")
    public ApiResponse<ProductDetailResponse> detail(@PathVariable String slugOrId) {
        return ApiResponse.ok(productService.getPublicDetail(slugOrId));
    }
}
