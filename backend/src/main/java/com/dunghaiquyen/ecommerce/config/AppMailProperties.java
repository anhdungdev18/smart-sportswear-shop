package com.dunghaiquyen.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * App-level mail config only - "provider" (which MailService bean to
 * activate) and "from" (the address SmtpMailService sends as). Everything
 * else SMTP-related (host/port/username/password/auth/starttls) is plain
 * Spring Boot spring.mail.* config, already auto-bound by
 * MailSenderAutoConfiguration - duplicating those keys under app.mail would
 * just be two names for the same setting.
 */
@ConfigurationProperties(prefix = "app.mail")
public record AppMailProperties(String provider, String from) {
}
