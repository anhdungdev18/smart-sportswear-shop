package com.dunghaiquyen.ecommerce.modules.review.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.review.dto.ReviewListQuery;
import com.dunghaiquyen.ecommerce.modules.review.dto.ReviewResponse;
import com.dunghaiquyen.ecommerce.modules.review.dto.UpdateReviewStatusRequest;
import com.dunghaiquyen.ecommerce.modules.review.service.ReviewService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** ADMIN only - the spec only mentions admin moderating reviews, not sales/warehouse staff. */
@RestController
@RequestMapping("/api/v1/admin/reviews")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ApiResponse<List<ReviewResponse>> list(@ModelAttribute ReviewListQuery query) {
        ReviewService.ListResult result = reviewService.listAdmin(query);
        return ApiResponse.ok(result.items(), result.meta());
    }

    @GetMapping("/{reviewId}")
    public ApiResponse<ReviewResponse> detail(@PathVariable UUID reviewId) {
        return ApiResponse.ok(reviewService.getAdminDetail(reviewId));
    }

    @PatchMapping("/{reviewId}/status")
    public ApiResponse<ReviewResponse> updateStatus(
            @PathVariable UUID reviewId, @Valid @RequestBody UpdateReviewStatusRequest request) {
        return ApiResponse.ok("Review status updated", reviewService.updateStatus(reviewId, request));
    }
}
