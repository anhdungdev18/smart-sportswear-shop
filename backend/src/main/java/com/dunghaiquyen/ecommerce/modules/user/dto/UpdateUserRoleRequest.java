package com.dunghaiquyen.ecommerce.modules.user.dto;

import com.dunghaiquyen.ecommerce.modules.user.entity.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(@NotNull(message = "Role is required") UserRole role) {
}
