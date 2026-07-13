package com.dunghaiquyen.ecommerce.modules.recommendation.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RebuildAssociationRulesRequest;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RebuildAssociationRulesResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.service.AssociationRuleMiningService;
import com.dunghaiquyen.ecommerce.modules.recommendation.service.RecommendationCacheService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/recommendations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRecommendationController {

    private final AssociationRuleMiningService associationRuleMiningService;
    private final RecommendationCacheService recommendationCacheService;

    public AdminRecommendationController(
            AssociationRuleMiningService associationRuleMiningService,
            RecommendationCacheService recommendationCacheService) {
        this.associationRuleMiningService = associationRuleMiningService;
        this.recommendationCacheService = recommendationCacheService;
    }

    @PostMapping("/association-rules/rebuild")
    public ApiResponse<RebuildAssociationRulesResponse> rebuildAssociationRules(
            @Valid @RequestBody(required = false) RebuildAssociationRulesRequest request) {

        RebuildAssociationRulesResponse response =
                associationRuleMiningService.rebuildAssociationRules(request);

        recommendationCacheService.evictAllRecommendationCaches();

        return ApiResponse.ok("Association rules rebuilt", response);
    }
}