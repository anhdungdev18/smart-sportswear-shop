package com.dunghaiquyen.ecommerce.modules.setting.dto;

import com.dunghaiquyen.ecommerce.modules.setting.entity.SettingValueType;

/** No description/updatedBy/timestamps - admin-only operational metadata. Only settings with isPublic=true are ever returned through this shape. */
public record PublicSiteSettingResponse(String settingKey, String settingValue, SettingValueType valueType) {
}
