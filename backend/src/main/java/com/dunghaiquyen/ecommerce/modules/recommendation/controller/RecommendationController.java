package com.dunghaiquyen.ecommerce.modules.recommendation.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RecommendationResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.service.RecommendationService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{productId}/frequently-bought-together")
    public ApiResponse<RecommendationResponse> frequentlyBoughtTogether(
            @PathVariable UUID productId,
            @RequestParam(required = false) Integer limit) {

        return ApiResponse.ok(
                recommendationService.getFrequentlyBoughtTogether(productId, limit)
        );
    }
}