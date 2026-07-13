package com.dunghaiquyen.ecommerce.infra.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.dunghaiquyen.ecommerce.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class ForecastDemoDataSeederIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SeedDataService coreSeeder;

    @Autowired
    private ForecastDemoDataSeeder forecastSeeder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void seed_isReproducibleAndIdempotent_withoutTouchingNonDemoOrders() {
        coreSeeder.seed();
        int nonDemoBefore = count("select count(*) from orders where note is distinct from '[FORECAST_DEMO]'");

        ForecastDemoDataSeeder.SeedSummary first = forecastSeeder.seed();
        long firstDemand = sumValidDemand();

        assertThat(first.variants()).isEqualTo(30);
        assertThat(first.orders()).isEqualTo(3000);
        assertThat(first.historyDays()).isEqualTo(180);
        assertThat(count("select count(*) from product_variants where sku like 'FD-%'")).isEqualTo(30);
        assertThat(count("select count(*) from orders where note = '[FORECAST_DEMO]'"))
                .isEqualTo(3000);
        assertThat(count("select count(*) from orders where note = '[FORECAST_DEMO]' and order_status = 'CANCELLED'"))
                .isPositive();
        assertThat(count("select count(*) from orders where note = '[FORECAST_DEMO]' and order_status = 'PENDING_CONFIRMATION'"))
                .isPositive();

        ForecastDemoDataSeeder.SeedSummary second = forecastSeeder.seed();

        assertThat(second).isEqualTo(first);
        assertThat(count("select count(*) from orders where note = '[FORECAST_DEMO]'"))
                .isEqualTo(3000);
        assertThat(sumValidDemand()).isEqualTo(firstDemand);
        assertThat(count("select count(*) from orders where note is distinct from '[FORECAST_DEMO]'"))
                .isEqualTo(nonDemoBefore);
    }

    private int count(String sql) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class);
        return value == null ? 0 : value;
    }

    private long sumValidDemand() {
        Long value = jdbcTemplate.queryForObject("""
                select coalesce(sum(oi.quantity), 0)
                from order_items oi
                join orders o on o.id = oi.order_id
                where o.note = '[FORECAST_DEMO]'
                  and o.order_status in ('CONFIRMED', 'PACKING', 'SHIPPING', 'DELIVERED')
                """, Long.class);
        return value == null ? 0 : value;
    }
}
