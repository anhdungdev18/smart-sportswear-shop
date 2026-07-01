package com.dunghaiquyen.ecommerce.config;

import com.cloudinary.Cloudinary;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The Cloudinary bean only exists when app.storage.provider=cloudinary is
 * explicitly chosen (CloudinaryImageStorageService requires it) - same
 * "explicitly selected but misconfigured must fail fast at startup, not
 * silently" philosophy as SmtpMailService. A fresh checkout with no
 * Cloudinary credentials at all never creates this bean and never hits this
 * check - the rest of the app (including the legacy manual-URL image flow)
 * works exactly as before.
 */
@Configuration
public class CloudinaryConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.storage", name = "provider", havingValue = "cloudinary")
    public Cloudinary cloudinary(AppCloudinaryProperties properties) {
        if (isBlank(properties.cloudName()) || isBlank(properties.apiKey()) || isBlank(properties.apiSecret())) {
            throw new IllegalStateException(
                    "app.storage.provider=cloudinary requires CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY and "
                            + "CLOUDINARY_API_SECRET to all be set");
        }
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", properties.cloudName());
        config.put("api_key", properties.apiKey());
        config.put("api_secret", properties.apiSecret());
        config.put("secure", true);
        return new Cloudinary(config);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
