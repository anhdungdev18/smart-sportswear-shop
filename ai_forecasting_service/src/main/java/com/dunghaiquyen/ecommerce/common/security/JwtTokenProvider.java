package com.dunghaiquyen.ecommerce.common.security;
import com.dunghaiquyen.ecommerce.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
@Component
public class JwtTokenProvider {
    private final SecretKey accessKey;
    public JwtTokenProvider(JwtProperties properties) { this.accessKey = Keys.hmacShaKeyFor(properties.accessSecret().getBytes(StandardCharsets.UTF_8)); }
    public Optional<AccessTokenClaims> parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(accessKey).build().parseSignedClaims(token).getPayload();
            if (!"access".equals(claims.get("type", String.class))) return Optional.empty();
            String role = claims.get("role", String.class);
            if (role == null || role.isBlank()) return Optional.empty();
            return Optional.of(new AccessTokenClaims(UUID.fromString(claims.getSubject()), role));
        } catch (JwtException | IllegalArgumentException ex) { return Optional.empty(); }
    }
    public record AccessTokenClaims(UUID userId, String role) { }
}