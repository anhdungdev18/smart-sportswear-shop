package com.dunghaiquyen.ecommerce.modules.brand.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.brand.dto.BrandCreateRequest;
import com.dunghaiquyen.ecommerce.modules.brand.dto.BrandResponse;
import com.dunghaiquyen.ecommerce.modules.brand.dto.BrandUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.brand.service.BrandService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/brands")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBrandController {

    private final BrandService brandService;

    public AdminBrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BrandResponse>> create(@Valid @RequestBody BrandCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Brand created", brandService.create(request)));
    }

    @PatchMapping("/{id}")
    public ApiResponse<BrandResponse> update(@PathVariable UUID id, @Valid @RequestBody BrandUpdateRequest request) {
        return ApiResponse.ok("Brand updated", brandService.update(id, request));
    }
}
