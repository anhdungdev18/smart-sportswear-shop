package com.dunghaiquyen.ecommerce.modules.notification.service;

import com.dunghaiquyen.ecommerce.modules.notification.dto.NotificationStreamMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * Receives notifications published on the Redis channel (from any instance) and
 * delivers them to THIS instance's live SSE streams. Registered against the
 * channel by {@link NotificationRedisConfig}; a malformed or unroutable message
 * is logged and dropped rather than allowed to break the listener.
 */
@Component
public class NotificationStreamSubscriber implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationStreamSubscriber.class);

    private final ObjectMapper objectMapper;
    private final NotificationStreamService streamService;

    public NotificationStreamSubscriber(ObjectMapper objectMapper, NotificationStreamService streamService) {
        this.objectMapper = objectMapper;
        this.streamService = streamService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            NotificationStreamMessage envelope =
                    objectMapper.readValue(message.getBody(), NotificationStreamMessage.class);
            streamService.push(
                    envelope.userId(), NotificationStreamService.EVENT_NOTIFICATION, envelope.payload());
        } catch (Exception ex) {
            log.warn("Dropping malformed notification stream message: {}", ex.getMessage());
        }
    }
}
