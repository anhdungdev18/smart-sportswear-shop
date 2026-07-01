package com.dunghaiquyen.ecommerce.modules.notification.dto;

import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationChannel;
import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationStatus;
import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationType;
import java.time.LocalDate;
import java.util.UUID;

/** Admin notification history query (Phase O): all filters optional. */
public record AdminNotificationListQuery(
        Integer page,
        Integer limit,
        NotificationType type,
        NotificationStatus status,
        NotificationChannel channel,
        UUID userId,
        UUID orderId,
        LocalDate dateFrom,
        LocalDate dateTo) {
}
