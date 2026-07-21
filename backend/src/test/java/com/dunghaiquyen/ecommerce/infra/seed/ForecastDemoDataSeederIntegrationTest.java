package com.dunghaiquyen.ecommerce.infra.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.dunghaiquyen.ecommerce.config.AppForecastDemoProperties;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.dunghaiquyen.ecommerce.AbstractIntegrationTest;

public class ForecastDemoDataSeederIntegrationTest extends AbstractIntegrationTest {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.forecast-demo.enabled", () -> "true");
        registry.add("app.forecast-demo.random-seed", () -> "2026");
        registry.add("app.forecast-demo.anchor-date", () -> "2026-07-16");
        registry.add("app.forecast-demo.history-days", () -> "180");
        registry.add("app.forecast-demo.order-count", () -> "3000");
        registry.add("app.forecast-demo.variant-count", () -> "9");
        registry.add("app.forecast-demo.marker", () -> "[FORECAST_DEMO]");
        registry.add("app.forecast-demo.cleanup-before-seed", () -> "true");
        registry.add("app.jwt.access-secret", () -> "test-access-secret-32-bytes-long");
        registry.add("app.jwt.refresh-secret", () -> "test-refresh-secret-32-bytes-long");
        registry.add("app.vnpay.hash-secret", () -> "test-vnpay-secret-32-bytes-long");
    }

    @Autowired
    private ForecastDemoDataSeeder seeder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SeedDataService seedDataService;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("delete from order_items");
        jdbcTemplate.update("delete from orders");
        jdbcTemplate.update("delete from product_variants");
        jdbcTemplate.update("delete from products");
        jdbcTemplate.update("delete from users");

        // Seed core users and variants
        seedDataService.seed();
    }

    @Test
    void testSeederIsIdempotentAndMatchesCount() {
        seeder.seed();

        Long ordersCount1 = jdbcTemplate.queryForObject("select count(*) from orders where note = '[FORECAST_DEMO]'", Long.class);
        Long itemsCount1 = jdbcTemplate.queryForObject("select count(*) from order_items where order_id in (select id from orders where note = '[FORECAST_DEMO]')", Long.class);

        assertThat(ordersCount1).isEqualTo(3000L);
        assertThat(itemsCount1).isGreaterThan(3000L);

        // Calculate a sum or hash of items
        Long totalQuantity1 = jdbcTemplate.queryForObject("select coalesce(sum(quantity), 0) from order_items where order_id in (select id from orders where note = '[FORECAST_DEMO]')", Long.class);

        // Run seed again (cleanup-before-seed is true)
        seeder.seed();

        Long ordersCount2 = jdbcTemplate.queryForObject("select count(*) from orders where note = '[FORECAST_DEMO]'", Long.class);
        Long itemsCount2 = jdbcTemplate.queryForObject("select count(*) from order_items where order_id in (select id from orders where note = '[FORECAST_DEMO]')", Long.class);
        Long totalQuantity2 = jdbcTemplate.queryForObject("select coalesce(sum(quantity), 0) from order_items where order_id in (select id from orders where note = '[FORECAST_DEMO]')", Long.class);

        assertThat(ordersCount2).isEqualTo(ordersCount1);
        assertThat(itemsCount2).isEqualTo(itemsCount1);
        assertThat(totalQuantity2).isEqualTo(totalQuantity1);

        // Verify some variants are used
        Long variantCount = jdbcTemplate.queryForObject("select count(distinct variant_id) from order_items where order_id in (select id from orders where note = '[FORECAST_DEMO]')", Long.class);
        assertThat(variantCount).isGreaterThan(0L);
        assertThat(variantCount).isLessThanOrEqualTo(50L);
    }
}
