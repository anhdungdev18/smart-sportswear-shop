package com.dunghaiquyen.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.order")
public record AppOrderProperties(Integer pendingPaymentExpiryMinutes) {
    public int effectivePendingPaymentExpiryMinutes() {
        return pendingPaymentExpiryMinutes == null || pendingPaymentExpiryMinutes < 1
                ? 30 : pendingPaymentExpiryMinutes;
    }
}
