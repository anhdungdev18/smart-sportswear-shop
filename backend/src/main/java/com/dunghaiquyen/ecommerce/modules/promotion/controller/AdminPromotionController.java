package com.dunghaiquyen.ecommerce.modules.promotion.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.promotion.dto.PromotionCreateRequest;
import com.dunghaiquyen.ecommerce.modules.promotion.dto.PromotionResponse;
import com.dunghaiquyen.ecommerce.modules.promotion.dto.UpdatePromotionStatusRequest;
import com.dunghaiquyen.ecommerce.modules.promotion.service.PromotionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/promotions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPromotionController {

    private final PromotionService promotionService;

    public AdminPromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public ApiResponse<List<PromotionResponse>> list() {
        return ApiResponse.ok(promotionService.listAll());
    }

    @PostMapping
    public ApiResponse<PromotionResponse> create(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody PromotionCreateRequest request) {
        return ApiResponse.ok("Promotion created", promotionService.create(request, principal.getUserId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<PromotionResponse> update(
            @PathVariable UUID id, @Valid @RequestBody PromotionCreateRequest request) {
        return ApiResponse.ok("Promotion updated", promotionService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<PromotionResponse> updateStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdatePromotionStatusRequest request) {
        return ApiResponse.ok(promotionService.updateStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        promotionService.delete(id);
        return ApiResponse.ok(null);
    }
}
