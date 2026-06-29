package com.dunghaiquyen.ecommerce.modules.auth.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.auth.dto.AuthResponse;
import com.dunghaiquyen.ecommerce.modules.auth.dto.AuthTokensResponse;
import com.dunghaiquyen.ecommerce.modules.auth.dto.LoginRequest;
import com.dunghaiquyen.ecommerce.modules.auth.dto.LogoutRequest;
import com.dunghaiquyen.ecommerce.modules.auth.dto.RefreshRequest;
import com.dunghaiquyen.ecommerce.modules.auth.dto.RegisterRequest;
import com.dunghaiquyen.ecommerce.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Register successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthTokensResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthTokensResponse tokens = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed", tokens));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(
            @AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken(), principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Logout successful", Map.of()));
    }
}
