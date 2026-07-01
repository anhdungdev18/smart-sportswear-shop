package com.dunghaiquyen.ecommerce.modules.returns.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.returns.dto.CreateReturnRequest;
import com.dunghaiquyen.ecommerce.modules.returns.dto.ReturnResponse;
import com.dunghaiquyen.ecommerce.modules.returns.service.ReturnService;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;

/** Customer self-service - hasRole('CUSTOMER') mirrors POST /api/v1/orders and /api/v1/orders/{id}/cancel's own restriction. */
@RestController
@RequestMapping("/api/v1/returns")
@PreAuthorize("hasRole('CUSTOMER')")
public class ReturnController {

    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReturnResponse>> create(
            @AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody CreateReturnRequest request) {
        ReturnResponse response = returnService.createReturn(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Return requested", response));
    }

    @GetMapping("/me")
    public ApiResponse<List<ReturnResponse>> listMine(@AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.ok(returnService.listMine(principal.getUserId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReturnResponse> detail(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable UUID id) {
        return ApiResponse.ok(returnService.getDetail(id, principal.getUserId()));
    }

    /** Only valid while still REQUESTED - see ReturnService's ALLOWED_TRANSITIONS. */
    @PostMapping("/{id}/cancel")
    public ApiResponse<ReturnResponse> cancel(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable UUID id) {
        return ApiResponse.ok("Return cancelled", returnService.cancelOwnReturn(id, principal.getUserId()));
    }
}
