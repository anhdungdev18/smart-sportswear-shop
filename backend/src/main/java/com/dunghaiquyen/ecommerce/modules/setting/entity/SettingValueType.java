package com.dunghaiquyen.ecommerce.modules.setting.entity;

/** Exact set V3's chk_site_settings_value_type check constraint allows - drives SiteSettingService's minimal shape validation on write. */
public enum SettingValueType {
    STRING,
    NUMBER,
    BOOLEAN,
    JSON
}
