package com.dunghaiquyen.ecommerce.modules.recommendation.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.cart.web.CartIdentityResolver;
import com.dunghaiquyen.ecommerce.modules.cart.web.CartOwner;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RecommendationLogRequest;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.RecommendationLogResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.service.RecommendationLogService;
import com.dunghaiquyen.ecommerce.modules.recommendation.service.RecommendationEventRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecommendationLogController {

    private final RecommendationLogService recommendationLogService;
    private final RecommendationEventRateLimiter rateLimiter;
    private final CartIdentityResolver identityResolver;

    public RecommendationLogController(
            RecommendationLogService recommendationLogService,
            RecommendationEventRateLimiter rateLimiter,
            CartIdentityResolver identityResolver) {
        this.recommendationLogService = recommendationLogService;
        this.rateLimiter = rateLimiter;
        this.identityResolver = identityResolver;
    }

    @PostMapping("/api/v1/recommendations/logs")
    public ResponseEntity<ApiResponse<RecommendationLogResponse>> logRecommendationEvent(
            HttpServletRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody RecommendationLogRequest body) {

        CartOwner owner = identityResolver.resolve(request, response, principal);
        rateLimiter.check(owner, request.getRemoteAddr());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        "Recommendation event logged",
                        recommendationLogService.logEvent(owner, body)
                ));
    }
}
