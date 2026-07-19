package com.dunghaiquyen.ecommerce.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    // Cache names
    public static final String CATEGORIES    = "categories";
    public static final String BRANDS        = "brands";
    public static final String BANNERS       = "banners";
    public static final String COLLECTIONS   = "collections";
    public static final String PRODUCTS_LIST = "products_list";
    public static final String REPORT_OVERVIEW   = "report_overview";
    public static final String REPORT_INVENTORY  = "report_inventory";
    public static final String REPORT_PRODUCTS   = "report_products";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCacheNames(List.of(
                CATEGORIES, BRANDS, BANNERS, COLLECTIONS, PRODUCTS_LIST,
                REPORT_OVERVIEW, REPORT_INVENTORY, REPORT_PRODUCTS));
        // TTL 5 min, max 500 entries per cache
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(500)
                .recordStats());
        return manager;
    }
}
