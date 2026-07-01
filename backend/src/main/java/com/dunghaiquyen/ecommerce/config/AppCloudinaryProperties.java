package com.dunghaiquyen.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Only consumed by CloudinaryConfig - app.storage.provider (not bound here) decides whether that bean is even created. */
@ConfigurationProperties(prefix = "app.cloudinary")
public record AppCloudinaryProperties(String cloudName, String apiKey, String apiSecret) {
}
