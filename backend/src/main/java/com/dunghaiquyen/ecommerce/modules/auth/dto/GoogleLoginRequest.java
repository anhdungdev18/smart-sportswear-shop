package com.dunghaiquyen.ecommerce.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * The "credential" field is a Google ID token (JWT) produced by the
 * frontend via Google One Tap, the Google Identity Services JS library, or
 * any OAuth2 flow with the openid scope. The backend verifies it against
 * Google's tokeninfo endpoint - see AuthService.loginWithGoogle.
 */
public record GoogleLoginRequest(@NotBlank(message = "credential is required") String credential) {
}
