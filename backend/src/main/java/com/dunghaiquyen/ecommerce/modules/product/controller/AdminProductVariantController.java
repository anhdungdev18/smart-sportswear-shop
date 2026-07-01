package com.dunghaiquyen.ecommerce.modules.product.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.product.dto.ProductVariantResponse;
import com.dunghaiquyen.ecommerce.modules.product.dto.VariantUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.product.service.ProductVariantService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Separate controller because the path does not nest under /admin/products/{id} (API_SPEC_PHASE1.md 9.8). */
@RestController
@RequestMapping("/api/v1/admin/variants")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductVariantController {

    private final ProductVariantService productVariantService;

    public AdminProductVariantController(ProductVariantService productVariantService) {
        this.productVariantService = productVariantService;
    }

    @PatchMapping("/{id}")
    public ApiResponse<ProductVariantResponse> update(
            @PathVariable UUID id, @Valid @RequestBody VariantUpdateRequest request) {
        return ApiResponse.ok("Variant updated", productVariantService.update(id, request));
    }
}
