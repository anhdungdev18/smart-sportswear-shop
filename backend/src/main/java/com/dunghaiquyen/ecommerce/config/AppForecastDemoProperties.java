package com.dunghaiquyen.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.forecast-demo")
public record AppForecastDemoProperties(
        boolean enabled,
        long randomSeed,
        int historyDays,
        int orderCount,
        int variantCount) {
}
