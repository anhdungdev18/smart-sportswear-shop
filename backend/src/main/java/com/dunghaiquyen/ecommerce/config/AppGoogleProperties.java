package com.dunghaiquyen.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Google OAuth2 configuration. clientId must match the Web Client ID in
 * Google Cloud Console → APIs & Services → Credentials (used to validate the
 * "aud" claim of incoming ID tokens). tokenInfoUrl is the Google endpoint that
 * validates ID tokens and returns user profile claims - externalised so tests
 * can point it at a WireMock stub rather than calling real Google.
 */
@ConfigurationProperties(prefix = "app.google")
public record AppGoogleProperties(String clientId, String tokenInfoUrl) {
}
