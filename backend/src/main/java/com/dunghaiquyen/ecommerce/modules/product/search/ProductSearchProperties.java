package com.dunghaiquyen.ecommerce.modules.product.search;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.product-search")
public record ProductSearchProperties(
        boolean enabled,
        String serviceUrl,
        String internalToken,
        int timeoutSeconds,
        int rateLimitPerMinute) {
}
