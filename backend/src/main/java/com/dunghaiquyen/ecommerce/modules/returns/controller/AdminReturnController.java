package com.dunghaiquyen.ecommerce.modules.returns.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.returns.dto.AdminReturnListQuery;
import com.dunghaiquyen.ecommerce.modules.returns.dto.CreateRefundRequest;
import com.dunghaiquyen.ecommerce.modules.returns.dto.RefundResponse;
import com.dunghaiquyen.ecommerce.modules.returns.dto.ReturnResponse;
import com.dunghaiquyen.ecommerce.modules.returns.dto.UpdateReturnStatusRequest;
import com.dunghaiquyen.ecommerce.modules.returns.service.ReturnService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Class-level role covers the whole return workflow (sales approves/rejects,
 * warehouse marks received) - the one step that actually moves money
 * (creating a refund) is narrowed to ADMIN/SALES_STAFF at the method level,
 * excluding WAREHOUSE_STAFF (a warehouse role has no business triggering a
 * financial refund).
 */
@RestController
@RequestMapping("/api/v1/admin/returns")
@PreAuthorize("hasAnyRole('ADMIN','SALES_STAFF','WAREHOUSE_STAFF')")
public class AdminReturnController {

    private final ReturnService returnService;

    public AdminReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @GetMapping
    public ApiResponse<List<ReturnResponse>> list(@ModelAttribute AdminReturnListQuery query) {
        ReturnService.ListResult<ReturnResponse> result = returnService.listForAdmin(query);
        return ApiResponse.ok(result.items(), result.meta());
    }

    @GetMapping("/{id}")
    public ApiResponse<ReturnResponse> detail(@PathVariable UUID id) {
        return ApiResponse.ok(returnService.getDetailForAdmin(id));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<ReturnResponse> updateStatus(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReturnStatusRequest request) {
        ReturnResponse response = returnService.updateStatus(id, request, principal.getUser());
        return ApiResponse.ok("Return status updated", response);
    }

    @GetMapping("/{id}/refunds")
    public ApiResponse<List<RefundResponse>> refunds(@PathVariable UUID id) {
        return ApiResponse.ok(returnService.listRefundsForReturn(id));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('ADMIN','SALES_STAFF')")
    public ResponseEntity<ApiResponse<RefundResponse>> createRefund(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @RequestBody(required = false) CreateRefundRequest request) {
        CreateRefundRequest effective = request != null ? request : new CreateRefundRequest(null);
        RefundResponse response = returnService.createRefund(id, effective, principal.getUser());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Refund created", response));
    }
}
