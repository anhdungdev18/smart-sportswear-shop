package com.dunghaiquyen.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String accessSecret,
        String refreshSecret,
        long accessTokenTtlMinutes,
        long refreshTokenTtlDays) {
}
