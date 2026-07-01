package com.dunghaiquyen.ecommerce.modules.promotion.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.promotion.dto.PromotionCreateRequest;
import com.dunghaiquyen.ecommerce.modules.promotion.dto.PromotionListQuery;
import com.dunghaiquyen.ecommerce.modules.promotion.dto.PromotionResponse;
import com.dunghaiquyen.ecommerce.modules.promotion.dto.PromotionUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.promotion.service.PromotionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public ApiResponse<List<PromotionResponse>> list(@ModelAttribute PromotionListQuery query) {
        PromotionService.ListResult result = promotionService.list(query);
        return ApiResponse.ok(result.items(), result.meta());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PromotionResponse>> create(@Valid @RequestBody PromotionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Promotion created", promotionService.create(request)));
    }

    @PatchMapping("/{id}")
    public ApiResponse<PromotionResponse> update(
            @PathVariable UUID id, @Valid @RequestBody PromotionUpdateRequest request) {
        return ApiResponse.ok("Promotion updated", promotionService.update(id, request));
    }
}
