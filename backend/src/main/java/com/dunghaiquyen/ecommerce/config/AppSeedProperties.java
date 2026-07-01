package com.dunghaiquyen.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.seed")
public record AppSeedProperties(Boolean enabled, String demoPassword) {
}
