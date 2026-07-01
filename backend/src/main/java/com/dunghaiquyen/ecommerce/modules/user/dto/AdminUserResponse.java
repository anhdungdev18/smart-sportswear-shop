package com.dunghaiquyen.ecommerce.modules.user.dto;

import com.dunghaiquyen.ecommerce.modules.user.entity.UserRole;
import com.dunghaiquyen.ecommerce.modules.user.entity.UserStatus;
import java.time.Instant;
import java.util.UUID;

/** Richer than the self-service UserResponse - admin also needs lastLoginAt/createdAt for triage. */
public record AdminUserResponse(
        UUID id,
        String fullName,
        String email,
        String phone,
        UserRole role,
        UserStatus status,
        Instant lastLoginAt,
        Instant createdAt) {
}
