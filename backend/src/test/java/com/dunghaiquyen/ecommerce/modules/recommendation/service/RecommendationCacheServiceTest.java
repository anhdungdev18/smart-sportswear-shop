package com.dunghaiquyen.ecommerce.modules.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RecommendationCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private RecommendationCacheService service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new RecommendationCacheService(redisTemplate, new ObjectMapper());
    }

    @Test
    void cacheKeys_includeCurrentGeneration() {
        when(valueOperations.get("recommendation:version")).thenReturn("7");

        String key = service.productFrequentlyBoughtTogetherKey(UUID.randomUUID(), 8);

        assertThat(key).startsWith("recommendation:v7:fbt:product:");
    }

    @Test
    void evictAll_incrementsGeneration_withoutBlockingKeysCommand() {
        service.evictAllRecommendationCaches();

        verify(valueOperations).increment("recommendation:version");
        verify(redisTemplate, never()).keys(anyString());
    }
}
