package com.dunghaiquyen.ecommerce.modules.notification.dto;

import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationChannel;
import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * allowedPlaceholders is derived (NotificationPlaceholders.allowedFor(type)),
 * not persisted - tells the admin exactly which {tokens} this type's send
 * call can actually fill in, so the FE can hint/autocomplete instead of the
 * admin guessing and saving a template that would otherwise go out with a
 * literal, un-substituted "{typo}" in it.
 */
public record NotificationTemplateResponse(
        UUID id,
        NotificationType type,
        NotificationChannel channel,
        String subject,
        String body,
        List<String> allowedPlaceholders,
        Instant createdAt,
        Instant updatedAt) {
}
