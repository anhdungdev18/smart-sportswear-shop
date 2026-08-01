package com.dunghaiquyen.ecommerce.visualsearch.api;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class VisualSearchRateLimiter {

    private record Window(long minute, int count) {
    }

    private final VisualSearchProperties properties;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public VisualSearchRateLimiter(VisualSearchProperties properties) {
        this.properties = properties;
    }

    public void check(String clientKey) {
        long minute = Instant.now().getEpochSecond() / 60;
        Window updated = windows.compute(clientKey, (key, current) ->
                current == null || current.minute() != minute
                        ? new Window(minute, 1)
                        : new Window(minute, current.count() + 1));
        if (updated.count() > Math.max(1, properties.rateLimitPerMinute())) {
            throw new BusinessRuleException(HttpStatus.TOO_MANY_REQUESTS, "Visual search rate limit exceeded");
        }
    }
}
