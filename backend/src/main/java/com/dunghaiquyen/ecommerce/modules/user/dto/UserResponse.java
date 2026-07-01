package com.dunghaiquyen.ecommerce.modules.user.dto;

import com.dunghaiquyen.ecommerce.modules.user.entity.LoginProvider;
import com.dunghaiquyen.ecommerce.modules.user.entity.UserRole;
import com.dunghaiquyen.ecommerce.modules.user.entity.UserStatus;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String fullName,
        String email,
        String phone,
        UserRole role,
        UserStatus status,
        LoginProvider loginProvider) {
}
