package com.dunghaiquyen.ecommerce.modules.user.entity;

/** Matches V9's chk_users_login_provider check constraint. LOCAL = email+password; GOOGLE = Google OAuth ID-token flow. */
public enum LoginProvider {
    LOCAL,
    GOOGLE
}
