package com.dunghaiquyen.ecommerce.modules.review.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.review.dto.CreateReviewRequest;
import com.dunghaiquyen.ecommerce.modules.review.dto.ReviewListQuery;
import com.dunghaiquyen.ecommerce.modules.review.dto.ReviewResponse;
import com.dunghaiquyen.ecommerce.modules.review.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET is public (already covered by SecurityConfig's PUBLIC_GET_ENDPOINTS for
 * /api/v1/products/**) and only ever returns APPROVED reviews. POST requires
 * a logged-in CUSTOMER who actually purchased the product (see ReviewService).
 * Phase N4 contract is productIdOrSlug, not UUID only, so the controller keeps
 * the raw path segment as String and lets ReviewService resolve it with the
 * same UUID-shaped-slug fallback rule already used by ProductService.
 */
@RestController
@RequestMapping("/api/v1/products/{productIdOrSlug}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ApiResponse<List<ReviewResponse>> list(
            @PathVariable String productIdOrSlug, @ModelAttribute ReviewListQuery query) {
        ReviewService.ListResult result = reviewService.listPublic(productIdOrSlug, query);
        return ApiResponse.ok(result.items(), result.meta());
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable String productIdOrSlug,
            @Valid @RequestBody CreateReviewRequest body) {
        ReviewResponse response = reviewService.create(principal.getUserId(), productIdOrSlug, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Review submitted", response));
    }
}
