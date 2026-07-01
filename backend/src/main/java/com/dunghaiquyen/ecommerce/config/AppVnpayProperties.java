package com.dunghaiquyen.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.vnpay")
public record AppVnpayProperties(String tmnCode, String hashSecret, String payUrl, String returnUrl, String callbackUrl) {
}
