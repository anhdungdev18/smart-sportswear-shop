package com.dunghaiquyen.ecommerce.modules.notification.config;

import com.dunghaiquyen.ecommerce.modules.notification.service.NotificationBroadcaster;
import com.dunghaiquyen.ecommerce.modules.notification.service.NotificationStreamSubscriber;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Subscribes this instance to the Redis notification channel so it delivers
 * notifications published by any node to its own SSE streams. Gated by
 * {@code app.notifications.stream.redis-enabled} (default on): turn it off for a
 * single-instance deployment with no Redis, where the broadcaster falls back to
 * pure local delivery.
 */
@Configuration
@ConditionalOnProperty(
        name = "app.notifications.stream.redis-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class NotificationRedisConfig {

    @Bean
    RedisMessageListenerContainer notificationStreamListenerContainer(
            RedisConnectionFactory connectionFactory, NotificationStreamSubscriber subscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(subscriber, new ChannelTopic(NotificationBroadcaster.CHANNEL));
        return container;
    }
}
