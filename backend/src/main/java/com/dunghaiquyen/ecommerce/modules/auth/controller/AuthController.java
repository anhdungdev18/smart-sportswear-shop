package com.dunghaiquyen.ecommerce.modules.auth.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.auth.dto.AuthResponse;
import com.dunghaiquyen.ecommerce.modules.auth.dto.AuthTokensResponse;
import com.dunghaiquyen.ecommerce.modules.auth.dto.ForgotPasswordRequest;
import com.dunghaiquyen.ecommerce.modules.auth.dto.GoogleLoginRequest;
import com.dunghaiquyen.ecommerce.modules.auth.dto.LoginRequest;
import com.dunghaiquyen.ecommerce.modules.auth.dto.LogoutRequest;
import com.dunghaiquyen.ecommerce.modules.auth.dto.RefreshRequest;
import com.dunghaiquyen.ecommerce.modules.auth.dto.RegisterRequest;
import com.dunghaiquyen.ecommerce.modules.auth.dto.ResetPasswordRequest;
import com.dunghaiquyen.ecommerce.modules.auth.service.AuthService;
import com.dunghaiquyen.ecommerce.modules.auth.service.PasswordResetService;
import com.dunghaiquyen.ecommerce.modules.cart.web.CartIdentityResolver;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            @CookieValue(name = CartIdentityResolver.SESSION_COOKIE_NAME, required = false) String guestSessionId) {
        AuthResponse response = authService.register(request, guestSessionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Register successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            @CookieValue(name = CartIdentityResolver.SESSION_COOKIE_NAME, required = false) String guestSessionId) {
        AuthResponse response = authService.login(request, guestSessionId);
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

    /** API_SPEC_PHASE1.md 3.5 - same response regardless of whether the email exists. */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request.email());
        return ResponseEntity.ok(ApiResponse.ok("If the email exists, a reset instruction has been sent", Map.of()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(ApiResponse.ok("Password reset successful", Map.of()));
    }

    /**
     * Google One Tap / OAuth2 login. Frontend obtains a Google ID token
     * (credential) via Google's client-side library, sends it here; backend
     * verifies it against Google's tokeninfo endpoint and issues our own JWT pair.
     * Response shape is identical to /login so the frontend can treat both flows
     * the same after this call.
     */
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request,
            @CookieValue(name = CartIdentityResolver.SESSION_COOKIE_NAME, required = false) String guestSessionId) {
        AuthResponse response = authService.loginWithGoogle(request.credential(), guestSessionId);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }
}
