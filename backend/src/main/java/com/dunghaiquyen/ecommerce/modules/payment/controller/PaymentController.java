package com.dunghaiquyen.ecommerce.modules.payment.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.payment.dto.CreatePaymentRequest;
import com.dunghaiquyen.ecommerce.modules.payment.dto.CreatePaymentResponse;
import com.dunghaiquyen.ecommerce.modules.payment.dto.PaymentResponse;
import com.dunghaiquyen.ecommerce.modules.payment.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API_SPEC_PHASE1.md section 8 - VNPay sandbox payment session + callback. */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CreatePaymentResponse>> create(
            @AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody CreatePaymentRequest request) {
        CreatePaymentResponse response = paymentService.createPaymentSession(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Payment session created", response));
    }

    /**
     * Public per API_SPEC_PHASE1.md 8.2 (already permitted in SecurityConfig's
     * PUBLIC_ANY_METHOD_ENDPOINTS) - the gateway calls this directly with no JWT,
     * auth here is the checksum verified inside PaymentService. Bound as a flat
     * @RequestParam map rather than a typed DTO: VNPay's payload is a flat
     * vnp_* key/value set (not JSON), and the handler needs to hash over
     * whatever set of params actually arrived, not a fixed shape.
     */
    @PostMapping("/callback")
    public ApiResponse<Map<String, Object>> callback(@RequestParam Map<String, String> params) {
        Map<String, Object> data = paymentService.handleCallback(params);
        return ApiResponse.ok("Callback processed", data);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','SALES_STAFF')")
    public ApiResponse<List<PaymentResponse>> getByOrder(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable UUID orderId) {
        List<PaymentResponse> response =
                paymentService.getPaymentsByOrder(orderId, principal.getUserId(), principal.getUser().getRole());
        return ApiResponse.ok(response);
    }
}
