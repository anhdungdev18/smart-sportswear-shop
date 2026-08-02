package com.dunghaiquyen.ecommerce.modules.checkout.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.checkout.dto.CheckoutPreviewRequest;
import com.dunghaiquyen.ecommerce.modules.checkout.dto.CheckoutPreviewResponse;
import com.dunghaiquyen.ecommerce.modules.checkout.service.CheckoutPreviewService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** hasRole('CUSTOMER') mirrors POST /api/v1/orders exactly - this previews what that exact call would do for this exact caller. */
@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {

    private final CheckoutPreviewService checkoutPreviewService;

    public CheckoutController(CheckoutPreviewService checkoutPreviewService) {
        this.checkoutPreviewService = checkoutPreviewService;
    }

    @PostMapping("/preview")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<CheckoutPreviewResponse> preview(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody(required = false) CheckoutPreviewRequest request) {
        CheckoutPreviewRequest effective = request != null ? request : new CheckoutPreviewRequest(null, null);
        return ApiResponse.ok(checkoutPreviewService.preview(principal.getUserId(), effective));
    }
}
