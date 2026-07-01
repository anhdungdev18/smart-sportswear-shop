package com.dunghaiquyen.ecommerce.modules.notification.entity;

/** Only EMAIL exists this phase - the enum (rather than a bare "email" string) is what lets SMS/push be added later without a schema change. */
public enum NotificationChannel {
    EMAIL
}
