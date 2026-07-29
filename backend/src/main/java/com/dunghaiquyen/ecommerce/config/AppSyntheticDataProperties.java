package com.dunghaiquyen.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "synthetic.data")
public record AppSyntheticDataProperties(
        boolean allowed,
        String environment
) {
    public boolean isSafeEnvironment() {
        return "demo".equalsIgnoreCase(environment) || "development".equalsIgnoreCase(environment);
    }
}