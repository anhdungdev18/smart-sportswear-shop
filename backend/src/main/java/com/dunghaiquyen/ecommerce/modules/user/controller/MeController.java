package com.dunghaiquyen.ecommerce.modules.user.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.user.dto.UpdateMeRequest;
import com.dunghaiquyen.ecommerce.modules.user.dto.UserResponse;
import com.dunghaiquyen.ecommerce.modules.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Self-service only - GET/PATCH /api/v1/me. Any authenticated role may call
 * this (CUSTOMER, SALES_STAFF, WAREHOUSE_STAFF, ADMIN all manage their own
 * profile the same way); admin user management is a separate module.
 */
@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<UserResponse> getMe(@AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.ok(userService.getMe(principal.getUserId()));
    }

    @PatchMapping
    public ApiResponse<UserResponse> updateMe(
            @AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody UpdateMeRequest request) {
        return ApiResponse.ok(userService.updateMe(principal.getUserId(), request));
    }
}
