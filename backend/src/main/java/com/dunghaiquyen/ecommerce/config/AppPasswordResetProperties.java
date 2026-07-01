package com.dunghaiquyen.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.password-reset")
public record AppPasswordResetProperties(Integer tokenTtlMinutes, String resetUrl) {
}
