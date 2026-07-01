package com.dunghaiquyen.ecommerce.modules.notification.dto;

import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationStatus;
import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationType;

/** Self-service query (GET /api/v1/notifications/me) - userId is always the caller, never a request param. */
public record NotificationListQuery(Integer page, Integer limit, NotificationType type, NotificationStatus status) {
}
