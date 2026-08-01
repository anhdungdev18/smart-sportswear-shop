package com.dunghaiquyen.ecommerce.visualsearch.api;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.visual-search")
public record VisualSearchProperties(
        boolean enabled,
        String serviceUrl,
        String internalToken,
        int requestTimeoutSeconds,
        int rateLimitPerMinute) {
}
