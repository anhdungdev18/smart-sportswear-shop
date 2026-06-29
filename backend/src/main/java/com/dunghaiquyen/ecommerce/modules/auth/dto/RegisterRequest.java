package com.dunghaiquyen.ecommerce.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Intentionally has no "role" field: register always creates UserRole.CUSTOMER
 * in AuthService, the client has no way to request a different role.
 */
public record RegisterRequest(

        @NotBlank(message = "Full name is required")
        @Size(max = 150, message = "Full name must be at most 150 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email is invalid")
        @Size(max = 255, message = "Email must be at most 255 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
                message = "Password must be at least 8 characters and contain a letter and a digit")
        String password,

        @Pattern(regexp = "^[0-9+\\-() ]{8,20}$", message = "Phone is invalid")
        String phone) {
}
