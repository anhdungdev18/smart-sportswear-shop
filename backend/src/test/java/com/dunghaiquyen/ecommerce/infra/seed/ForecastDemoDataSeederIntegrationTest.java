package com.dunghaiquyen.ecommerce.infra.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.dunghaiquyen.ecommerce.AbstractIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public class ForecastDemoDataSeederIntegrationTest extends AbstractIntegrationTest {
    private static final String MARKER = "[FORECAST_DEMO_V2_TEST]";

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("synthetic.data.allowed", () -> "true");
        registry.add("synthetic.data.environment", () -> "demo");
        registry.add("app.forecast-demo.enabled", () -> "true");
        registry.add("app.forecast-demo.random-seed", () -> "20260725");
        registry.add("app.forecast-demo.anchor-date", () -> "2026-07-24");
        registry.add("app.forecast-demo.history-days", () -> "60");
        registry.add("app.forecast-demo.order-count", () -> "240");
        registry.add("app.forecast-demo.variant-count", () -> "9");
        registry.add("app.forecast-demo.marker", () -> MARKER);
        registry.add("app.forecast-demo.cleanup-before-seed", () -> "true");
        registry.add("app.forecast-demo.smooth-variants", () -> "1");
        registry.add("app.forecast-demo.normal-variants", () -> "1");
        registry.add("app.forecast-demo.slow-variants", () -> "1");
        registry.add("app.forecast-demo.intermittent-variants", () -> "1");
        registry.add("app.forecast-demo.erratic-variants", () -> "1");
        registry.add("app.forecast-demo.growing-variants", () -> "1");
        registry.add("app.forecast-demo.declining-variants", () -> "1");
        registry.add("app.forecast-demo.new-item-variants", () -> "1");
        registry.add("app.forecast-demo.no-demand-variants", () -> "1");
        registry.add("app.forecast-demo.weekend-multiplier", () -> "1.30");
        registry.add("app.forecast-demo.promotion-multiplier", () -> "3.00");
        registry.add("app.forecast-demo.promotion-day-of-month", () -> "15");
        registry.add("app.forecast-demo.new-item-history-days", () -> "14");
        registry.add("app.forecast-demo.cancel-rate", () -> "0.10");
        registry.add("app.forecast-demo.inventory-history-enabled", () -> "true");
        registry.add("app.forecast-demo.inventory-history-days", () -> "60");
        registry.add("app.forecast-demo.supplier-count", () -> "3");
        registry.add("app.forecast-demo.min-lead-time-days", () -> "7");
        registry.add("app.forecast-demo.max-lead-time-days", () -> "21");
        registry.add("app.forecast-demo.default-service-level", () -> "0.95");
        registry.add("app.forecast-demo.ground-truth-enabled", () -> "true");
        registry.add("app.forecast-demo.scenario-version", () -> "synthetic-v2-test");
        registry.add("app.jwt.access-secret", () -> "test-access-secret-32-bytes-long");
        registry.add("app.jwt.refresh-secret", () -> "test-refresh-secret-32-bytes-long");
        registry.add("app.vnpay.hash-secret", () -> "test-vnpay-secret-32-bytes-long");
    }

    @Autowired
    private ForecastDemoDataSeeder seeder;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @BeforeEach
    void setUp() {
        jdbcTemplate.update("delete from product_reviews");
        jdbcTemplate.update("delete from notifications");
        jdbcTemplate.update("delete from payments");
        jdbcTemplate.update("delete from inventory_transactions");
        jdbcTemplate.update("delete from order_items");
        jdbcTemplate.update("delete from orders");
        jdbcTemplate.update("delete from cart_items");
        jdbcTemplate.update("delete from carts");
        jdbcTemplate.update("delete from addresses");
        jdbcTemplate.update("delete from inventory_policies");
        jdbcTemplate.update("delete from forecast_demo_scenarios");
        jdbcTemplate.update("delete from product_variants");
        jdbcTemplate.update("delete from product_images");
        jdbcTemplate.update("delete from product_collections");
        jdbcTemplate.update("delete from products");
        jdbcTemplate.update("delete from collections");
        jdbcTemplate.update("delete from categories");
        jdbcTemplate.update("delete from brands");
        jdbcTemplate.update("delete from users");
        seedMinimalCatalog();
    }

    @Test
    void syntheticSeederV2CreatesReproducibleGroundTruthAndPolicies() {
        seeder.seed();

        Long ordersCount1 = count("select count(*) from orders where note = ?", MARKER);
        Long itemsCount1 = count("select count(*) from order_items where order_id in (select id from orders where note = ?)", MARKER);
        Long totalQuantity1 = count("select coalesce(sum(quantity), 0) from order_items where order_id in (select id from orders where note = ?)", MARKER);
        Long scenarioCount1 = count("select count(*) from forecast_demo_scenarios where marker = ?", MARKER);

        assertThat(ordersCount1).isEqualTo(240L);
        assertThat(itemsCount1).isEqualTo(240L);
        assertThat(scenarioCount1).isEqualTo(9L);

        assertThat(count("select count(distinct demand_profile) from forecast_demo_scenarios where marker = ?", MARKER)).isEqualTo(9L);
        assertThat(count("select count(*) from forecast_demo_scenarios where marker = ? and demand_profile = 'NO_DEMAND' and expected_total_units = 0 and expected_valid_units = 0", MARKER)).isEqualTo(1L);
        assertThat(count("select count(*) from forecast_demo_scenarios where marker = ? and supplier_name is not null and lead_time_days is not null and minimum_order_quantity is not null and pack_size is not null", MARKER)).isEqualTo(9L);
        assertThat(count("select count(distinct supplier_name) from forecast_demo_scenarios where marker = ?", MARKER)).isGreaterThan(1L);
        assertThat(count("select count(distinct lead_time_days) from forecast_demo_scenarios where marker = ?", MARKER)).isGreaterThan(1L);
        assertThat(count("select count(distinct minimum_order_quantity) from forecast_demo_scenarios where marker = ?", MARKER)).isGreaterThan(1L);
        assertThat(count("select count(distinct pack_size) from forecast_demo_scenarios where marker = ?", MARKER)).isGreaterThan(1L);
        assertThat(count("select count(*) from order_items where line_total <> unit_price_snapshot * quantity")).isZero();
        assertThat(count("select count(*) from orders where note = ? and order_status = 'CANCELLED'", MARKER)).isGreaterThan(0L);
        assertThat(count("select count(*) from orders where note = ? and data_source = 'DEMO'", MARKER)).isEqualTo(240L);

        Long validUnits = count("""
                select coalesce(sum(oi.quantity), 0)
                from order_items oi
                join orders o on o.id = oi.order_id
                where o.note = ? and o.order_status <> 'CANCELLED'
                """, MARKER);
        Long scenarioValidUnits = count("select coalesce(sum(expected_valid_units), 0) from forecast_demo_scenarios where marker = ?", MARKER);
        assertThat(scenarioValidUnits).isEqualTo(validUnits);

        seeder.seed();

        assertThat(count("select count(*) from orders where note = ?", MARKER)).isEqualTo(ordersCount1);
        assertThat(count("select count(*) from order_items where order_id in (select id from orders where note = ?)", MARKER)).isEqualTo(itemsCount1);
        assertThat(count("select coalesce(sum(quantity), 0) from order_items where order_id in (select id from orders where note = ?)", MARKER)).isEqualTo(totalQuantity1);
        assertThat(count("select count(*) from forecast_demo_scenarios where marker = ?", MARKER)).isEqualTo(scenarioCount1);
    }

    @Test
    void cleanupOnlyDeletesRecordsWithExactMarker() {
        UUID unrelatedOrderId = UUID.randomUUID();
        UUID userId = jdbcTemplate.queryForObject("select id from users limit 1", UUID.class);
        jdbcTemplate.update("""
                insert into orders (
                    id, order_code, user_id, order_status, payment_method, payment_status,
                    note, address_snapshot_json, subtotal_amount, shipping_fee, discount_amount, total_amount,
                    created_at, updated_at
                ) values (?, 'OTHER-MARKER-ORDER', ?, 'CONFIRMED', 'COD', 'UNPAID',
                    '[FORECAST_DEMO_V2_OTHER]', '{}'::jsonb, 0, 0, 0, 0, now(), now())
                """, unrelatedOrderId, userId);

        seeder.seed();
        seeder.seed();

        assertThat(count("select count(*) from orders where note = '[FORECAST_DEMO_V2_OTHER]'")).isEqualTo(1L);
        assertThat(count("select count(*) from orders where note = ?", MARKER)).isEqualTo(240L);
    }

    private void seedMinimalCatalog() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID categoryId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID brandId = UUID.fromString("00000000-0000-0000-0000-000000000003");

        jdbcTemplate.update("""
                insert into users (id, full_name, email, password_hash, role, status, created_at, updated_at)
                values (?, 'Forecast Demo Customer', 'forecast-demo-test@example.com', 'hash', 'CUSTOMER', 'ACTIVE', now(), now())
                """, userId);
        jdbcTemplate.update("""
                insert into categories (id, name, slug, status, created_at, updated_at)
                values (?, 'Forecast Test Category', 'forecast-test-category', 'ACTIVE', now(), now())
                """, categoryId);
        jdbcTemplate.update("""
                insert into brands (id, name, slug, status, created_at, updated_at)
                values (?, 'Forecast Test Brand', 'forecast-test-brand', 'ACTIVE', now(), now())
                """, brandId);

        for (int i = 1; i <= 9; i++) {
            UUID productId = UUID.nameUUIDFromBytes(("forecast-demo-product-" + i).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            UUID variantId = UUID.nameUUIDFromBytes(("forecast-demo-variant-" + i).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            jdbcTemplate.update("""
                    insert into products (
                        id, category_id, brand_id, name, slug, short_description, description,
                        gender, sport_type, product_type, status, is_featured, attributes, created_at, updated_at
                    ) values (?, ?, ?, ?, ?, 'Forecast demo product', 'Forecast demo product',
                        'UNISEX', 'RUNNING', 'APPAREL', 'ACTIVE', false, '{}'::jsonb, now(), now())
                    """, productId, categoryId, brandId, "Forecast Demo Product " + i, "forecast-demo-product-" + i);
            jdbcTemplate.update("""
                    insert into product_variants (
                        id, product_id, sku, size, color, price, compare_at_price,
                        stock_quantity, reserved_quantity, version, status, created_at, updated_at
                    ) values (?, ?, ?, 'M', ?, ?, null, 1000, 0, 1, 'ACTIVE', now(), now())
                    """, variantId, productId, String.format("FDV2-TEST-%02d", i), "Color " + i, java.math.BigDecimal.valueOf(100000 + i * 1000L));
        }
    }

    private Long count(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, Long.class, args);
    }
}
