package com.dunghaiquyen.ecommerce.infra.seed;

import com.dunghaiquyen.ecommerce.config.AppForecastDemoProperties;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductVariantRepository;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import com.dunghaiquyen.ecommerce.modules.user.repository.UserRepository;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!prod & !production")
@ConditionalOnProperty(prefix = "app.forecast-demo", name = "enabled", havingValue = "true")
public class ForecastDemoDataSeeder {
    private static final Logger log = LoggerFactory.getLogger(ForecastDemoDataSeeder.class);
    private final JdbcTemplate jdbcTemplate;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final AppForecastDemoProperties properties;

    public ForecastDemoDataSeeder(JdbcTemplate jdbcTemplate, ProductVariantRepository variantRepository, UserRepository userRepository, AppForecastDemoProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
        this.properties = properties;
    }

    @Transactional
    public void seed() {
        if (!properties.enabled()) return;
        if (properties.anchorDate() == null) {
            log.error("Anchor date must be configured when forecast demo is enabled");
            return;
        }

        if (properties.cleanupBeforeSeed()) {
            cleanup();
        } else {
            Long count = jdbcTemplate.queryForObject("select count(*) from orders where note = ?", Long.class, properties.marker());
            if (count != null && count > 0) {
                log.info("Forecast demo data already seeded, count={}", count);
                return;
            }
        }

        List<ProductVariant> allVariants = variantRepository.findAll();
        List<ProductVariant> variants = allVariants.stream()
                .filter(v -> v.getStatus() == com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus.ACTIVE)
                .sorted((v1, v2) -> {
                    int c = v1.getSku().compareTo(v2.getSku());
                    if (c == 0) return v1.getId().compareTo(v2.getId());
                    return c;
                })
                .limit(properties.variantCount())
                .toList();

        if (variants.size() < properties.variantCount()) {
            log.warn("Not enough active variants for demo seed. Expected: {}, Found: {}", properties.variantCount(), variants.size());
            return;
        }
        
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            log.warn("No users found. Cannot seed orders.");
            return;
        }

        log.info("Starting to seed {} forecast demo orders using seed {}...", properties.orderCount(), properties.randomSeed());
        long startTime = System.currentTimeMillis();

        Random random = new Random(properties.randomSeed());
        LocalDate anchorDate = properties.anchorDate();
        LocalDate startDate = anchorDate.minusDays(properties.historyDays());
        
        int totalDays = properties.historyDays();

        int n = variants.size();
        int fastCount = (int) Math.max(1, n * 0.2);
        int normalCount = (int) Math.max(1, n * 0.3);
        int slowCount = (int) Math.max(1, n * 0.3);
        
        if (fastCount + normalCount + slowCount >= n) {
            fastCount = Math.max(1, n / 4);
            normalCount = Math.max(1, n / 4);
            slowCount = Math.max(1, n / 4);
        }
        
        List<ProductVariant> fastVariants = variants.subList(0, fastCount);
        List<ProductVariant> normalVariants = variants.subList(fastCount, fastCount + normalCount);
        List<ProductVariant> slowVariants = variants.subList(fastCount + normalCount, fastCount + normalCount + slowCount);
        List<ProductVariant> intVariants = variants.subList(fastCount + normalCount + slowCount, n);

        List<Object[]> orderBatch = new ArrayList<>();
        List<Object[]> orderItemBatch = new ArrayList<>();

        for (int i = 0; i < properties.orderCount(); i++) {
            UUID orderId = UUID.randomUUID();
            
            // Random day in history
            int dayOffset = random.nextInt(totalDays);
            LocalDate orderDate = startDate.plusDays(dayOffset);
            
            // Add some time to the day
            Timestamp createdAt = Timestamp.valueOf(orderDate.atTime(random.nextInt(24), random.nextInt(60), random.nextInt(60)));
            
            String status = determineStatus(random);
            User user = users.get(random.nextInt(users.size()));
            String orderCode = "FD-" + orderId.toString().substring(0, 8).toUpperCase();
            
            long totalAmount = 0;
            int numItems = determineNumItems(random);
            
            for (int j = 0; j < numItems; j++) {
                ProductVariant variant = selectVariantForOrder(random, fastVariants, normalVariants, slowVariants, intVariants, orderDate);
                int qty = determineQuantityForProfile(random, getProfile(variant, fastVariants, normalVariants, slowVariants));
                
                BigDecimal price = variant.getPrice() != null ? variant.getPrice() : BigDecimal.ZERO;
                totalAmount += price.longValue() * qty;
                
                orderItemBatch.add(new Object[]{
                    UUID.randomUUID(), orderId, variant.getProduct().getId(), variant.getId(), qty, variant.getProduct().getName(),
                    variant.getSku(), variant.getSize(), variant.getColor(), price, price.multiply(BigDecimal.valueOf(qty))
                });
            }

            orderBatch.add(new Object[]{
                orderId, orderCode, user.getId(), status, "COD", "UNPAID",
                properties.marker(), "{}", totalAmount, 0, 0, totalAmount,
                createdAt, createdAt
            });
        }
        
        jdbcTemplate.batchUpdate("""
            insert into orders (
                id, order_code, user_id, order_status, payment_method, payment_status,
                note, address_snapshot_json, subtotal_amount, shipping_fee, discount_amount, total_amount,
                created_at, updated_at
            ) values (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?, ?)
        """, orderBatch);

        jdbcTemplate.batchUpdate("""
            insert into order_items (
                id, order_id, product_id, variant_id, quantity, product_name_snapshot,
                sku_snapshot, size_snapshot, color_snapshot, unit_price_snapshot, line_total
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, orderItemBatch);

        long duration = System.currentTimeMillis() - startTime;
        log.info("Finished seeding {} orders and {} order items in {}ms with random seed {}.", 
            orderBatch.size(), orderItemBatch.size(), duration, properties.randomSeed());
    }

    private void cleanup() {
        log.info("Cleaning up existing forecast demo data...");
        int itemsDeleted = jdbcTemplate.update("""
            delete from order_items where order_id in (
                select id from orders where note = ?
            )
        """, properties.marker());
        int ordersDeleted = jdbcTemplate.update("delete from orders where note = ?", properties.marker());
        log.info("Deleted {} order items and {} orders.", itemsDeleted, ordersDeleted);
    }
    
    private String determineStatus(Random random) {
        double p = random.nextDouble();
        if (p < 0.05) return "CANCELLED";
        if (p < 0.08) return "PENDING_CONFIRMATION";
        if (p < 0.40) return "DELIVERED";
        if (p < 0.70) return "CONFIRMED";
        if (p < 0.90) return "PACKING";
        return "SHIPPING";
    }
    
    private int determineNumItems(Random random) {
        return 1 + random.nextInt(3);
    }
    
    private String getProfile(ProductVariant variant, List<ProductVariant> fast, List<ProductVariant> normal, List<ProductVariant> slow) {
        if (fast.contains(variant)) return "FAST";
        if (normal.contains(variant)) return "NORMAL";
        if (slow.contains(variant)) return "SLOW";
        return "INTERMITTENT";
    }
    
    private int determineQuantityForProfile(Random random, String profile) {
        return switch (profile) {
            case "FAST" -> 2 + random.nextInt(4); // 2-5
            case "NORMAL" -> 1 + random.nextInt(3); // 1-3
            case "SLOW" -> 1 + random.nextInt(2); // 1-2
            default -> 1;
        };
    }

    private ProductVariant selectVariantForOrder(Random random, List<ProductVariant> fast, List<ProductVariant> normal, List<ProductVariant> slow, List<ProductVariant> intVariants, LocalDate orderDate) {
        // Implement logic to simulate demand profiles
        // Weekend effect: 30% more likely to buy fast/normal
        boolean isWeekend = orderDate.getDayOfWeek().getValue() >= 6;
        
        // Promotion spike: e.g., day of month is 15th
        boolean isPromotion = orderDate.getDayOfMonth() == 15;
        
        double p = random.nextDouble();
        if (isPromotion) {
            if (p < 0.6) return fast.get(random.nextInt(fast.size()));
            if (p < 0.9) return normal.get(random.nextInt(normal.size()));
            return slow.get(random.nextInt(slow.size()));
        }
        
        if (isWeekend) {
            if (p < 0.5) return fast.get(random.nextInt(fast.size()));
            if (p < 0.8) return normal.get(random.nextInt(normal.size()));
            if (p < 0.95) return slow.get(random.nextInt(slow.size()));
            return intVariants.get(random.nextInt(intVariants.size()));
        }
        
        // Weekday
        if (p < 0.3) return fast.get(random.nextInt(fast.size()));
        if (p < 0.6) return normal.get(random.nextInt(normal.size()));
        if (p < 0.85) return slow.get(random.nextInt(slow.size()));
        return intVariants.get(random.nextInt(intVariants.size()));
    }
}
