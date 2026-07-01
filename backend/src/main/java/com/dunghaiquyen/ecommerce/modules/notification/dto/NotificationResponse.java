package com.dunghaiquyen.ecommerce.modules.notification.dto;

import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationChannel;
import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationStatus;
import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID userId,
        UUID orderId,
        NotificationType type,
        NotificationChannel channel,
        String recipient,
        String subject,
        String body,
        NotificationStatus status,
        String errorMessage,
        Instant createdAt,
        Instant sentAt,
        UUID resendOfId,
        int resendCount,
        Instant lastResendAt) {
}
