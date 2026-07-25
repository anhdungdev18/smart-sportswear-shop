package com.dunghaiquyen.ecommerce.config;

import java.util.Locale;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.forecast")
public record ForecastDataSourceProperties(String dataSource) {
    public ForecastDataSourceProperties {
        dataSource = normalize(dataSource == null || dataSource.isBlank() ? "REAL" : dataSource);
    }

    private static String normalize(String value) {
        String normalized = value.toUpperCase(Locale.ROOT);
        if (!normalized.equals("DEMO") && !normalized.equals("REAL") && !normalized.equals("IMPORTED")) {
            throw new IllegalArgumentException("app.forecast.data-source must be DEMO, REAL, or IMPORTED");
        }
        return normalized;
    }
}
