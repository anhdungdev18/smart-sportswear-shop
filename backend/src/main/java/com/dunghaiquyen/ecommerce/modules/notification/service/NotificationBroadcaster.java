package com.dunghaiquyen.ecommerce.modules.notification.service;

import com.dunghaiquyen.ecommerce.modules.notification.dto.NotificationResponse;
import com.dunghaiquyen.ecommerce.modules.notification.dto.NotificationStreamMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Fans a newly-created notification out to live SSE streams across ALL app
 * instances.
 *
 * <p>The single-instance {@link NotificationStreamService} only knows about
 * emitters on its own JVM; a notification created on instance B never reaches a
 * user whose browser is streaming from instance A. This broadcaster closes that
 * gap by publishing to a Redis channel every instance subscribes to
 * ({@code NotificationStreamSubscriber}), so whichever node holds the stream
 * delivers it.
 *
 * <p><b>Graceful degradation:</b> if Redis is disabled by config, or the publish
 * throws (Redis down), delivery falls back to this instance's own emitters — a
 * single-node deployment keeps working with no Redis at all. Delivery on the
 * happy path happens purely through the subscription (including on the
 * publishing node itself), so a notification is pushed to each stream exactly once.
 */
@Service
public class NotificationBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(NotificationBroadcaster.class);

    public static final String CHANNEL = "notifications:stream";

    private final boolean redisEnabled;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationStreamService streamService;

    public NotificationBroadcaster(
            @Value("${app.notifications.stream.redis-enabled:true}") boolean redisEnabled,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            NotificationStreamService streamService) {
        this.redisEnabled = redisEnabled;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.streamService = streamService;
    }

    public void broadcast(UUID userId, NotificationResponse payload) {
        if (redisEnabled) {
            try {
                String json = objectMapper.writeValueAsString(new NotificationStreamMessage(userId, payload));
                redisTemplate.convertAndSend(CHANNEL, json);
                return;
            } catch (Exception ex) {
                log.warn("Redis publish failed, delivering locally instead: {}", ex.getMessage());
            }
        }
        streamService.push(userId, NotificationStreamService.EVENT_NOTIFICATION, payload);
    }
}
