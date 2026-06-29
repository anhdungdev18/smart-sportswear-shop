package com.dunghaiquyen.ecommerce.modules.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** PATCH /api/v1/me: every field is optional, null means "leave unchanged". */
public record UpdateMeRequest(

        @Size(max = 150, message = "Full name must be at most 150 characters")
        String fullName,

        @Pattern(regexp = "^[0-9+\\-() ]{8,20}$", message = "Phone is invalid")
        String phone) {
}
