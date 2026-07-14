package com.dunghaiquyen.ecommerce.modules.recommendation.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.cart.web.CartIdentityResolver;
import com.dunghaiquyen.ecommerce.modules.cart.web.CartOwner;
import com.dunghaiquyen.ecommerce.modules.recommendation.dto.CartRecommendationResponse;
import com.dunghaiquyen.ecommerce.modules.recommendation.service.RecommendationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
public class CartRecommendationController {

    private final RecommendationService recommendationService;
    private final CartIdentityResolver identityResolver;

    public CartRecommendationController(
            RecommendationService recommendationService,
            CartIdentityResolver identityResolver) {
        this.recommendationService = recommendationService;
        this.identityResolver = identityResolver;
    }

    @GetMapping("/recommendations")
    public ApiResponse<CartRecommendationResponse> cartRecommendations(
            HttpServletRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) Integer limit) {

        CartOwner owner = identityResolver.resolve(request, response, principal);

        return ApiResponse.ok(
                recommendationService.getCartRecommendations(owner, limit)
        );
    }
}