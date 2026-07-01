package com.dunghaiquyen.ecommerce.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Same password strength policy as RegisterRequest - one rule, not two to keep in sync. */
public record ResetPasswordRequest(
        @NotBlank(message = "Token is required") String token,
        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
                message = "Password must be at least 8 characters and contain a letter and a digit")
        String newPassword) {
}
