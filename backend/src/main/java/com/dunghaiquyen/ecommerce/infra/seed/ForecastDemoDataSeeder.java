package com.dunghaiquyen.ecommerce.infra.seed;

import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductVariantRepository;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import com.dunghaiquyen.ecommerce.modules.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.forecast-demo", name = "enabled", havingValue = "true")
public class ForecastDemoDataSeeder {
    private static final Logger log = LoggerFactory.getLogger(ForecastDemoDataSeeder.class);
    private final JdbcClient jdbc;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    public ForecastDemoDataSeeder(JdbcClient jdbc, ProductVariantRepository variantRepository, UserRepository userRepository) {
        this.jdbc = jdbc;
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
    }

    public void seed() {
        // Only run if properties allow, but for demo we can check if orders already seeded
        Long count = jdbc.sql("select count(*) from orders where note = '[FORECAST_DEMO]'").query(Long.class).single();
        if (count != null && count > 0) {
            log.info("Forecast demo data already seeded, count={}", count);
            return;
        }

        List<ProductVariant> variants = variantRepository.findAll();
        if (variants.isEmpty()) return;
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) return;

        log.info("Starting to seed 3000 forecast demo orders...");
        Random random = new Random(2026);
        Instant end = Instant.now();
        Instant start = end.minus(180, ChronoUnit.DAYS);

        int batchSize = 0;
        for (int i = 0; i < 3000; i++) {
            UUID orderId = UUID.randomUUID();
            long offsetSeconds = (long) (random.nextDouble() * 180 * 24 * 3600);
            Instant createdAt = start.plusSeconds(offsetSeconds);
            String status = random.nextDouble() < 0.1 ? "CANCELLED" : "DELIVERED";
            User user = users.get(random.nextInt(users.size()));
            String orderCode = "FD-" + orderId.toString().substring(0, 8).toUpperCase();

            jdbc.sql("""
                insert into orders (
                    id, order_code, user_id, order_status, payment_method, payment_status,
                    note, address_snapshot_json, subtotal_amount, shipping_fee, discount_amount, total_amount,
                    created_at, updated_at
                )
                values (
                    :id, :orderCode, :userId, :status, 'COD', 'UNPAID',
                    '[FORECAST_DEMO]', '{}'::jsonb, 0, 0, 0, 0,
                    :createdAt, :createdAt
                )
            """)
            .param("id", orderId)
            .param("orderCode", orderCode)
            .param("userId", user.getId())
            .param("status", status)
            .param("createdAt", java.sql.Timestamp.from(createdAt))
            .update();

            int numItems = 1 + random.nextInt(3);
            long totalAmount = 0;
            for (int j = 0; j < numItems; j++) {
                ProductVariant variant = variants.get(random.nextInt(variants.size()));
                int qty = 1 + random.nextInt(5);
                BigDecimal price = variant.getPrice() != null ? variant.getPrice() : BigDecimal.ZERO;
                totalAmount += price.longValue() * qty;
                
                jdbc.sql("""
                    insert into order_items (
                        id, order_id, variant_id, quantity, product_name_snapshot,
                        sku_snapshot, size_snapshot, color_snapshot, unit_price_snapshot
                    )
                    values (
                        :id, :orderId, :variantId, :qty, :pName,
                        :sku, :size, :color, :price
                    )
                """)
                .param("id", UUID.randomUUID())
                .param("orderId", orderId)
                .param("variantId", variant.getId())
                .param("qty", qty)
                .param("pName", variant.getProduct().getName())
                .param("sku", variant.getSku())
                .param("size", variant.getSize())
                .param("color", variant.getColor())
                .param("price", price)
                .update();
            }
            
            jdbc.sql("update orders set subtotal_amount = :total, total_amount = :total where id = :id")
                .param("total", totalAmount)
                .param("id", orderId)
                .update();
                
            batchSize++;
            if (batchSize % 500 == 0) {
                log.info("Seeded {} forecast demo orders...", batchSize);
            }
        }
        log.info("Finished seeding 3000 forecast demo orders.");
    }
}
