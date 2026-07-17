package com.dunghaiquyen.ecommerce.modules.notification.dto;

import java.util.UUID;

/**
 * Envelope published on the Redis notification channel so every app instance can
 * fan a freshly-created notification out to the SSE streams it holds — carries
 * the target user plus the exact payload the client receives.
 */
public record NotificationStreamMessage(UUID userId, NotificationResponse payload) {
}
