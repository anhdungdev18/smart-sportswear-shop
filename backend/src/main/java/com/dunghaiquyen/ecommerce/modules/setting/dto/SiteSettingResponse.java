package com.dunghaiquyen.ecommerce.modules.setting.dto;

import com.dunghaiquyen.ecommerce.modules.setting.entity.SettingValueType;
import java.time.Instant;
import java.util.UUID;

public record SiteSettingResponse(
        UUID id,
        String settingKey,
        String settingValue,
        SettingValueType valueType,
        String description,
        boolean isPublic,
        UUID updatedBy,
        Instant createdAt,
        Instant updatedAt) {
}
