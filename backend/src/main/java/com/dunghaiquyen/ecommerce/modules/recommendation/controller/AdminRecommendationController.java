package com.dunghaiquyen.ecommerce.modules.recommendation.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RebuildAssociationRulesRequest;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RebuildAssociationRulesResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RecommendationLogSummaryResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.service.AssociationRuleMiningService;
import com.dunghaiquyen.ecommerce.modules.recommendation.service.RecommendationCacheService;
import com.dunghaiquyen.ecommerce.modules.recommendation.service.RecommendationLogService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/recommendations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRecommendationController {

    private final AssociationRuleMiningService associationRuleMiningService;
    private final RecommendationCacheService recommendationCacheService;
    private final RecommendationLogService recommendationLogService;

    public AdminRecommendationController(
            AssociationRuleMiningService associationRuleMiningService,
            RecommendationCacheService recommendationCacheService,
            RecommendationLogService recommendationLogService) {
        this.associationRuleMiningService = associationRuleMiningService;
        this.recommendationCacheService = recommendationCacheService;
        this.recommendationLogService = recommendationLogService;
    }

    @PostMapping("/association-rules/rebuild")
    public ApiResponse<RebuildAssociationRulesResponse> rebuildAssociationRules(
            @Valid @RequestBody(required = false) RebuildAssociationRulesRequest request) {

        RebuildAssociationRulesResponse response =
                associationRuleMiningService.rebuildAssociationRules(request);

        recommendationCacheService.evictAllRecommendationCaches();

        return ApiResponse.ok("Association rules rebuilt", response);
    }

    @GetMapping("/logs/summary")
    public ApiResponse<RecommendationLogSummaryResponse> recommendationLogSummary(
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) Integer limit) {

        return ApiResponse.ok(
                recommendationLogService.getSummary(days, limit)
        );
    }
}