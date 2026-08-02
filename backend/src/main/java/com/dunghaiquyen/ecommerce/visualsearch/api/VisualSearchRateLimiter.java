package com.dunghaiquyen.ecommerce.visualsearch.api;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class VisualSearchRateLimiter {

    private record Window(long minute, int count) {
    }

    private final VisualSearchProperties properties;
    private final Cache<String, Window> windows = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(2))
            .maximumSize(100_000)
            .build();

    public VisualSearchRateLimiter(VisualSearchProperties properties) {
        this.properties = properties;
    }

    public void check(String clientKey) {
        long minute = Instant.now().getEpochSecond() / 60;
        Window updated = windows.asMap().compute(clientKey, (key, current) ->
                current == null || current.minute() != minute
                        ? new Window(minute, 1)
                        : new Window(minute, current.count() + 1));
        if (updated.count() > Math.max(1, properties.rateLimitPerMinute())) {
            throw new BusinessRuleException(HttpStatus.TOO_MANY_REQUESTS, "Visual search rate limit exceeded");
        }
    }
}
