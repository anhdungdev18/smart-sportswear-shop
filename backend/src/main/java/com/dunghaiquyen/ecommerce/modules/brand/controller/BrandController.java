package com.dunghaiquyen.ecommerce.modules.brand.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.brand.dto.BrandResponse;
import com.dunghaiquyen.ecommerce.modules.brand.service.BrandService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public, read-only: only ACTIVE brands are visible (API_SPEC_PHASE1.md 5.2). */
@RestController
@RequestMapping("/api/v1/brands")
public class BrandController {

    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public ApiResponse<List<BrandResponse>> list() {
        return ApiResponse.ok(brandService.listActive());
    }
}
