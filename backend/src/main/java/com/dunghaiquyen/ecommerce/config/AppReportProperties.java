package com.dunghaiquyen.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.report")
public record AppReportProperties(Integer lowStockThreshold) {
}
