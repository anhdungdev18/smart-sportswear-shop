package com.dunghaiquyen.ecommerce.modules.replenishment;

import static org.assertj.core.api.Assertions.assertThat;

import com.dunghaiquyen.ecommerce.AbstractIntegrationTest;
import com.dunghaiquyen.ecommerce.common.time.AppTimeZone;
import com.dunghaiquyen.ecommerce.modules.replenishment.service.DailyDemandService;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class DailyDemandIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DailyDemandService dailyDemandService;

    @Test
    void aggregatesByVariant_excludesPendingAndCancelled_andFillsMissingDaysWithZero() {
        UUID suffix = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        UUID brandId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID variantA = UUID.randomUUID();
        UUID variantB = UUID.randomUUID();
        LocalDate from = LocalDate.of(2026, 1, 10);

        insertCatalog(userId, categoryId, brandId, productId, variantA, variantB, suffix);
        insertOrder(userId, productId, variantA, suffix, 1, from, "CONFIRMED", 2);
        insertOrder(userId, productId, variantA, suffix, 2, from, "CANCELLED", 9);
        insertOrder(userId, productId, variantA, suffix, 3, from, "PENDING_CONFIRMATION", 8);
        insertOrder(userId, productId, variantA, suffix, 4, from.plusDays(2), "DELIVERED", 3);
        insertOrder(userId, productId, variantB, suffix, 5, from.plusDays(1), "SHIPPING", 4);

        var result = dailyDemandService.getDailyDemand(List.of(variantA, variantB), from, from.plusDays(2));

        assertThat(result.get(variantA)).extracting(DailyDemandService.DailyDemandPoint::quantity)
                .containsExactly(2L, 0L, 3L);
        assertThat(result.get(variantB)).extracting(DailyDemandService.DailyDemandPoint::quantity)
                .containsExactly(0L, 4L, 0L);
        assertThat(result.get(variantA)).extracting(DailyDemandService.DailyDemandPoint::date)
                .containsExactly(from, from.plusDays(1), from.plusDays(2));
    }

    private void insertCatalog(
            UUID userId,
            UUID categoryId,
            UUID brandId,
            UUID productId,
            UUID variantA,
            UUID variantB,
            UUID suffix) {
        Timestamp now = Timestamp.from(fromLocal(LocalDate.of(2026, 1, 1)));
        jdbcTemplate.update("""
                insert into users (id, full_name, email, password_hash, role, status, created_at, updated_at)
                values (?, 'Forecast Test', ?, 'test', 'CUSTOMER', 'ACTIVE', ?, ?)
                """, userId, "forecast-" + suffix + "@example.com", now, now);
        jdbcTemplate.update("""
                insert into categories (id, name, slug, status, created_at, updated_at)
                values (?, 'Forecast Test', ?, 'ACTIVE', ?, ?)
                """, categoryId, "forecast-category-" + suffix, now, now);
        jdbcTemplate.update("""
                insert into brands (id, name, slug, status, created_at, updated_at)
                values (?, 'Forecast Test', ?, 'ACTIVE', ?, ?)
                """, brandId, "forecast-brand-" + suffix, now, now);
        jdbcTemplate.update("""
                insert into products (id, category_id, brand_id, name, slug, status, is_featured, created_at, updated_at)
                values (?, ?, ?, 'Forecast Test Product', ?, 'ACTIVE', false, ?, ?)
                """, productId, categoryId, brandId, "forecast-product-" + suffix, now, now);
        insertVariant(variantA, productId, "TEST-A-" + suffix, now);
        insertVariant(variantB, productId, "TEST-B-" + suffix, now);
    }

    private void insertVariant(UUID id, UUID productId, String sku, Timestamp now) {
        jdbcTemplate.update("""
                insert into product_variants (
                    id, product_id, sku, size, color, price, stock_quantity,
                    reserved_quantity, version, status, created_at, updated_at
                ) values (?, ?, ?, 'M', 'Black', 100000, 20, 0, 1, 'ACTIVE', ?, ?)
                """, id, productId, sku, now, now);
    }

    private void insertOrder(
            UUID userId,
            UUID productId,
            UUID variantId,
            UUID suffix,
            int sequence,
            LocalDate date,
            String status,
            int quantity) {
        UUID orderId = UUID.randomUUID();
        Timestamp createdAt = Timestamp.from(fromLocal(date));
        jdbcTemplate.update("""
                insert into orders (
                    id, order_code, user_id, address_snapshot_json,
                    subtotal_amount, shipping_fee, discount_amount, total_amount,
                    payment_method, order_status, payment_status, note, created_at, updated_at
                ) values (?, ?, ?, '{}'::jsonb, ?, 0, 0, ?, 'COD', ?, 'UNPAID', '[FORECAST_QUERY_TEST]', ?, ?)
                """, orderId, "FQ-" + suffix + "-" + sequence, userId,
                quantity * 100000, quantity * 100000, status, createdAt, createdAt);
        jdbcTemplate.update("""
                insert into order_items (
                    id, order_id, product_id, variant_id, product_name_snapshot,
                    sku_snapshot, size_snapshot, color_snapshot, unit_price_snapshot,
                    quantity, line_total
                ) values (?, ?, ?, ?, 'Forecast Test Product', ?, 'M', 'Black', 100000, ?, ?)
                """, UUID.randomUUID(), orderId, productId, variantId,
                "SNAP-" + variantId, quantity, quantity * 100000);
    }

    private java.time.Instant fromLocal(LocalDate date) {
        return date.atTime(LocalTime.NOON).atZone(AppTimeZone.ZONE).toInstant();
    }
}
