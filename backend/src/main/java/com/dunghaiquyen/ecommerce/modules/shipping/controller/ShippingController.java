package com.dunghaiquyen.ecommerce.modules.shipping.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.shipping.dto.ShippingFeePreviewRequest;
import com.dunghaiquyen.ecommerce.modules.shipping.dto.ShippingFeePreviewResponse;
import com.dunghaiquyen.ecommerce.modules.shipping.dto.ShippingMethodResponse;
import com.dunghaiquyen.ecommerce.modules.shipping.service.ShippingFeePreviewService;
import com.dunghaiquyen.ecommerce.modules.shipping.service.ShippingMethodService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipping")
public class ShippingController {

    private final ShippingMethodService shippingMethodService;
    private final ShippingFeePreviewService shippingFeePreviewService;

    public ShippingController(
            ShippingMethodService shippingMethodService, ShippingFeePreviewService shippingFeePreviewService) {
        this.shippingMethodService = shippingMethodService;
        this.shippingFeePreviewService = shippingFeePreviewService;
    }

    /** Public catalog read - see SecurityConfig.PUBLIC_GET_ENDPOINTS. */
    @GetMapping("/methods")
    public ApiResponse<List<ShippingMethodResponse>> methods() {
        return ApiResponse.ok(shippingMethodService.listAvailable());
    }

    /** hasRole('CUSTOMER') mirrors POST /api/v1/checkout/preview - fee depends on the caller's own current cart. */
    @PostMapping("/fee-preview")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<ShippingFeePreviewResponse> feePreview(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ShippingFeePreviewRequest request) {
        return ApiResponse.ok(shippingFeePreviewService.preview(principal.getUserId(), request));
    }
}
