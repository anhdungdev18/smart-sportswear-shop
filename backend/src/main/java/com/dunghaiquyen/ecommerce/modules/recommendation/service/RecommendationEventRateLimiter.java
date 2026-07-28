package com.dunghaiquyen.ecommerce.modules.recommendation.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.modules.cart.web.CartOwner;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RecommendationEventRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RecommendationEventRateLimiter.class);
    private static final long MAX_EVENTS_PER_MINUTE = 120;
    private static final Duration WINDOW_TTL = Duration.ofMinutes(2);
    private static final String KEY_PREFIX = "recommendation:event-rate:";

    private final StringRedisTemplate redisTemplate;

    public RecommendationEventRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void check(CartOwner owner, String clientAddress) {
        long minuteBucket = Instant.now().getEpochSecond() / 60;
        String ownerKey = owner.isUser()
                ? "user:" + owner.userId()
                : "session:" + owner.sessionId();

        enforce(KEY_PREFIX + ownerKey + ":" + minuteBucket);
        if (clientAddress != null && !clientAddress.isBlank()) {
            enforce(KEY_PREFIX + "ip:" + clientAddress + ":" + minuteBucket);
        }
    }

    private void enforce(String key) {
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, WINDOW_TTL);
            }
            if (count != null && count > MAX_EVENTS_PER_MINUTE) {
                throw new BusinessRuleException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Too many recommendation events"
                );
            }
        } catch (BusinessRuleException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            log.warn("Recommendation event rate limiter unavailable", ex);
        }
    }
}
