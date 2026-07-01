package com.dunghaiquyen.ecommerce.modules.user.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.user.dto.AdminUserListQuery;
import com.dunghaiquyen.ecommerce.modules.user.dto.AdminUserResponse;
import com.dunghaiquyen.ecommerce.modules.user.dto.UpdateUserRoleRequest;
import com.dunghaiquyen.ecommerce.modules.user.dto.UpdateUserStatusRequest;
import com.dunghaiquyen.ecommerce.modules.user.service.AdminUserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** PHASE1_SPEC.md 6.2 - admin user management. No endpoint shape is dictated by API_SPEC_PHASE1.md; this one is this phase's own. */
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ApiResponse<List<AdminUserResponse>> list(@ModelAttribute AdminUserListQuery query) {
        AdminUserService.ListResult<AdminUserResponse> result = adminUserService.listUsers(query);
        return ApiResponse.ok(result.items(), result.meta());
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminUserResponse> detail(@PathVariable UUID id) {
        return ApiResponse.ok(adminUserService.getUserDetail(id));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<AdminUserResponse> updateStatus(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        AdminUserResponse response = adminUserService.updateStatus(id, request, principal.getUserId());
        return ApiResponse.ok("User status updated", response);
    }

    @PatchMapping("/{id}/role")
    public ApiResponse<AdminUserResponse> updateRole(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        AdminUserResponse response = adminUserService.updateRole(id, request, principal.getUserId());
        return ApiResponse.ok("User role updated", response);
    }
}
