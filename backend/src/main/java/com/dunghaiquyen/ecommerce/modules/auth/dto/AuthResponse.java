package com.dunghaiquyen.ecommerce.modules.auth.dto;

import com.dunghaiquyen.ecommerce.modules.user.dto.UserResponse;

public record AuthResponse(UserResponse user, AuthTokensResponse tokens) {
}
