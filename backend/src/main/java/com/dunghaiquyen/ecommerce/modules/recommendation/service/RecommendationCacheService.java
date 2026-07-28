package com.dunghaiquyen.ecommerce.modules.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class RecommendationCacheService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationCacheService.class);

    private static final String KEY_PREFIX = "recommendation:";
    private static final String VERSION_KEY = KEY_PREFIX + "version";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RecommendationCacheService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public String productFrequentlyBoughtTogetherKey(UUID productId, int limit) {
        return versionedPrefix() + "fbt:product:" + productId + ":limit:" + limit;
    }

    public String cartRecommendationKey(UUID cartId, List<UUID> sourceProductIds, int limit) {
        String sortedProductIds = sourceProductIds.stream()
                .map(UUID::toString)
                .sorted()
                .collect(Collectors.joining(","));

        String productIdsHash = DigestUtils.md5DigestAsHex(
                sortedProductIds.getBytes(StandardCharsets.UTF_8)
        );

        return versionedPrefix() + "cart:" + cartId + ":products:" + productIdsHash + ":limit:" + limit;
    }

    public <T> Optional<T> get(String key, Class<T> responseType) {
        try {
            String json = redisTemplate.opsForValue().get(key);

            if (json == null || json.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(objectMapper.readValue(json, responseType));
        } catch (RuntimeException | JsonProcessingException ex) {
            log.warn("Failed to read recommendation cache. key={}", key, ex);
            return Optional.empty();
        }
    }

    public void put(String key, Object value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, CACHE_TTL);
        } catch (RuntimeException | JsonProcessingException ex) {
            log.warn("Failed to write recommendation cache. key={}", key, ex);
        }
    }

    public void evictAllRecommendationCaches() {
        try {
            redisTemplate.opsForValue().increment(VERSION_KEY);
        } catch (RuntimeException ex) {
            log.warn("Failed to evict recommendation caches", ex);
        }
    }

    private String versionedPrefix() {
        try {
            String version = redisTemplate.opsForValue().get(VERSION_KEY);
            return KEY_PREFIX + "v" + (version == null ? "0" : version) + ":";
        } catch (RuntimeException ex) {
            log.warn("Failed to read recommendation cache version", ex);
            return KEY_PREFIX + "v0:";
        }
    }
}
