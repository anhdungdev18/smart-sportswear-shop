package com.dunghaiquyen.ecommerce.modules.user.dto;

import com.dunghaiquyen.ecommerce.modules.user.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(@NotNull(message = "Status is required") UserStatus status) {
}
