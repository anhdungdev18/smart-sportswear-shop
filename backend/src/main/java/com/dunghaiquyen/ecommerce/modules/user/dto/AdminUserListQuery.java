package com.dunghaiquyen.ecommerce.modules.user.dto;

import com.dunghaiquyen.ecommerce.modules.user.entity.UserRole;
import com.dunghaiquyen.ecommerce.modules.user.entity.UserStatus;

/** Query params for GET /api/v1/admin/users - every field optional. */
public record AdminUserListQuery(Integer page, Integer limit, String keyword, UserRole role, UserStatus status) {
}
