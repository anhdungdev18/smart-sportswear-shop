package com.dunghaiquyen.ecommerce.modules.setting.dto;

import com.dunghaiquyen.ecommerce.modules.setting.entity.SettingValueType;
import jakarta.validation.constraints.NotNull;

/** settingKey itself is the path variable (PUT /api/v1/admin/settings/{key}), not part of the body - upsert by key, create-or-update in one call. */
public record UpsertSiteSettingRequest(
        String settingValue, @NotNull SettingValueType valueType, String description, boolean isPublic) {
}
