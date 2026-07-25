package com.dunghaiquyen.ecommerce.infra.seed;

import com.dunghaiquyen.ecommerce.config.AppForecastDemoProperties;
import com.dunghaiquyen.ecommerce.config.AppSyntheticDataProperties;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus;
import com.dunghaiquyen.ecommerce.modules.product.repository.ProductVariantRepository;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import com.dunghaiquyen.ecommerce.modules.user.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final List<String> VALID_DEMAND_STATUSES = List.of("CONFIRMED", "PACKING", "SHIPPING", "DELIVERED");

    private final JdbcTemplate jdbcTemplate;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final AppForecastDemoProperties properties;
    private final AppSyntheticDataProperties syntheticDataProperties;

    public ForecastDemoDataSeeder(
            JdbcTemplate jdbcTemplate,
            ProductVariantRepository variantRepository,
            UserRepository userRepository,
            AppForecastDemoProperties properties,
            AppSyntheticDataProperties syntheticDataProperties
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
        this.properties = properties;
        this.syntheticDataProperties = syntheticDataProperties;
    }

    @Transactional
    public void seed() {
        if (!properties.enabled()) {
            return;
        }
        validateConfiguration();

        if (properties.cleanupBeforeSeed()) {
            cleanup();
        } else {
            Long count = jdbcTemplate.queryForObject("select count(*) from orders where note = ?", Long.class, properties.marker());
            if (count != null && count > 0) {
                log.info("Forecast demo data already seeded, count={}", count);
                return;
            }
        }

        List<ProductVariant> variants = variantRepository.findAll().stream()
                .filter(v -> v.getStatus() == VariantStatus.ACTIVE)
                .sorted((v1, v2) -> {
                    int c = v1.getSku().compareTo(v2.getSku());
                    return c != 0 ? c : v1.getId().compareTo(v2.getId());
                })
                .limit(properties.variantCount())
                .toList();
        if (variants.size() < properties.variantCount()) {
            throw new IllegalStateException("Not enough active variants for forecast demo seed. expected="
                    + properties.variantCount() + ", actual=" + variants.size());
        }

        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new IllegalStateException("No users found. Cannot seed forecast demo orders.");
        }

        log.info("Starting Synthetic Seeder V2: orders={}, variants={}, seed={}, marker={}",
                properties.orderCount(), properties.variantCount(), properties.randomSeed(), properties.marker());
        long startTime = System.currentTimeMillis();

        Random random = new Random(properties.randomSeed());
        List<ScenarioVariant> scenarioVariants = assignProfiles(variants);
        List<ScenarioVariant> demandVariants = scenarioVariants.stream()
                .filter(v -> v.profile() != DemandProfile.NO_DEMAND)
                .toList();
        LocalDate startDate = properties.anchorDate().minusDays(properties.historyDays() - 1L);

        Map<UUID, ScenarioTotals> totals = new HashMap<>();
        for (ScenarioVariant scenario : scenarioVariants) {
            totals.put(scenario.variant().getId(), new ScenarioTotals());
        }

        List<Object[]> orderBatch = new ArrayList<>(properties.orderCount());
        List<Object[]> orderItemBatch = new ArrayList<>(properties.orderCount());

        for (int i = 0; i < properties.orderCount(); i++) {
            UUID orderId = nextUuid(random);
            LocalDate orderDate = randomOrderDate(random, startDate);
            Timestamp createdAt = Timestamp.valueOf(LocalDateTime.of(
                    orderDate,
                    java.time.LocalTime.of(random.nextInt(24), random.nextInt(60), random.nextInt(60))));
            String status = determineStatus(random);
            User user = users.get(random.nextInt(users.size()));
            String orderCode = "FDV2-" + properties.randomSeed() + "-" + String.format("%05d", i + 1);

            ScenarioVariant scenario = selectVariantForOrder(random, demandVariants, orderDate);
            int quantity = determineQuantity(random, scenario.profile());
            ProductVariant variant = scenario.variant();
            BigDecimal price = variant.getPrice() != null ? variant.getPrice() : BigDecimal.ZERO;
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(quantity));

            ScenarioTotals scenarioTotals = totals.get(variant.getId());
            scenarioTotals.totalUnits += quantity;
            if (VALID_DEMAND_STATUSES.contains(status)) {
                scenarioTotals.validUnits += quantity;
            }

            orderItemBatch.add(new Object[]{
                    nextUuid(random), orderId, variant.getProduct().getId(), variant.getId(), quantity,
                    variant.getProduct().getName(), variant.getSku(), variant.getSize(), variant.getColor(), price, lineTotal
            });
            orderBatch.add(new Object[]{
                    orderId, orderCode, user.getId(), status, "COD", "UNPAID",
                    properties.marker(), "{}", lineTotal, 0, 0, lineTotal,
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

        seedInventoryPoliciesAndScenarios(random, scenarioVariants, totals);

        long duration = System.currentTimeMillis() - startTime;
        log.info("Finished Synthetic Seeder V2: orders={}, items={}, scenarios={}, durationMs={}",
                orderBatch.size(), orderItemBatch.size(), scenarioVariants.size(), duration);
    }

    private void validateConfiguration() {
        if (!syntheticDataProperties.allowed()) {
            throw new IllegalStateException("SYNTHETIC_DATA_ALLOWED must be true before forecast demo seed can run");
        }
        if (!syntheticDataProperties.isSafeEnvironment()) {
            throw new IllegalStateException("Synthetic seed only supports demo/development environments, got: "
                    + syntheticDataProperties.environment());
        }
        if (properties.anchorDate() == null) {
            throw new IllegalStateException("FORECAST_DEMO_ANCHOR_DATE must be configured");
        }
        if (properties.marker() == null || properties.marker().isBlank()) {
            throw new IllegalStateException("FORECAST_DEMO_MARKER must not be blank");
        }
        if (properties.profileVariantCount() != properties.variantCount()) {
            throw new IllegalStateException("Forecast demo profile counts must sum to FORECAST_DEMO_VARIANT_COUNT. expected="
                    + properties.variantCount() + ", actual=" + properties.profileVariantCount());
        }
        if (properties.effectiveCancelRate().compareTo(BigDecimal.ZERO) < 0
                || properties.effectiveCancelRate().compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalStateException("FORECAST_DEMO_CANCEL_RATE must be in [0,1]");
        }
        if (properties.minLeadTimeDays() > properties.maxLeadTimeDays()) {
            throw new IllegalStateException("FORECAST_DEMO_MIN_LEAD_TIME_DAYS cannot exceed max lead time");
        }
        if (properties.newItemHistoryDays() > properties.historyDays()) {
            throw new IllegalStateException("FORECAST_DEMO_NEW_ITEM_HISTORY_DAYS cannot exceed history days");
        }
    }

    private void cleanup() {
        log.info("Cleaning up existing forecast demo data for marker {}...", properties.marker());
        int itemsDeleted = jdbcTemplate.update("""
            delete from order_items where order_id in (select id from orders where note = ?)
        """, properties.marker());
        int ordersDeleted = jdbcTemplate.update("delete from orders where note = ?", properties.marker());
        int policiesDeleted = jdbcTemplate.update("""
            delete from inventory_policies where variant_id in (
                select variant_id from forecast_demo_scenarios where marker = ?
            )
        """, properties.marker());
        int scenariosDeleted = jdbcTemplate.update("delete from forecast_demo_scenarios where marker = ?", properties.marker());
        log.info("Deleted {} order items, {} orders, {} inventory policies, {} scenarios.",
                itemsDeleted, ordersDeleted, policiesDeleted, scenariosDeleted);
    }

    private List<ScenarioVariant> assignProfiles(List<ProductVariant> variants) {
        List<ScenarioVariant> result = new ArrayList<>(properties.variantCount());
        int offset = 0;
        offset = addProfile(result, variants, offset, DemandProfile.SMOOTH, properties.smoothVariants());
        offset = addProfile(result, variants, offset, DemandProfile.NORMAL, properties.normalVariants());
        offset = addProfile(result, variants, offset, DemandProfile.SLOW, properties.slowVariants());
        offset = addProfile(result, variants, offset, DemandProfile.INTERMITTENT, properties.intermittentVariants());
        offset = addProfile(result, variants, offset, DemandProfile.ERRATIC, properties.erraticVariants());
        offset = addProfile(result, variants, offset, DemandProfile.GROWING, properties.growingVariants());
        offset = addProfile(result, variants, offset, DemandProfile.DECLINING, properties.decliningVariants());
        offset = addProfile(result, variants, offset, DemandProfile.NEW_ITEM, properties.newItemVariants());
        addProfile(result, variants, offset, DemandProfile.NO_DEMAND, properties.noDemandVariants());
        return result;
    }

    private int addProfile(List<ScenarioVariant> result, List<ProductVariant> variants, int offset, DemandProfile profile, int count) {
        for (int i = 0; i < count; i++) {
            result.add(new ScenarioVariant(variants.get(offset + i), profile));
        }
        return offset + count;
    }

    private LocalDate randomOrderDate(Random random, LocalDate startDate) {
        return startDate.plusDays(random.nextInt(properties.historyDays()));
    }

    private String determineStatus(Random random) {
        double cancelRate = properties.effectiveCancelRate().doubleValue();
        double p = random.nextDouble();
        if (p < cancelRate) return "CANCELLED";
        if (p < cancelRate + 0.03) return "PENDING_CONFIRMATION";
        if (p < 0.40) return "DELIVERED";
        if (p < 0.70) return "CONFIRMED";
        if (p < 0.90) return "PACKING";
        return "SHIPPING";
    }

    private ScenarioVariant selectVariantForOrder(Random random, List<ScenarioVariant> demandVariants, LocalDate orderDate) {
        EnumMap<DemandProfile, Double> weights = new EnumMap<>(DemandProfile.class);
        for (ScenarioVariant scenario : demandVariants) {
            double weight = scenario.profile().baseWeight();
            if (orderDate.getDayOfWeek() == DayOfWeek.SATURDAY || orderDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                weight *= properties.effectiveWeekendMultiplier().doubleValue();
            }
            if (orderDate.getDayOfMonth() == properties.promotionDayOfMonth()) {
                weight *= properties.effectivePromotionMultiplier().doubleValue();
            }
            if (scenario.profile() == DemandProfile.NEW_ITEM
                    && orderDate.isBefore(properties.anchorDate().minusDays(properties.newItemHistoryDays() - 1L))) {
                weight = 0.0;
            }
            if (scenario.profile() == DemandProfile.GROWING) {
                weight *= trendFactor(orderDate, true);
            }
            if (scenario.profile() == DemandProfile.DECLINING) {
                weight *= trendFactor(orderDate, false);
            }
            weights.merge(scenario.profile(), weight, Double::sum);
        }

        double totalWeight = 0.0;
        List<Double> itemWeights = new ArrayList<>(demandVariants.size());
        for (ScenarioVariant scenario : demandVariants) {
            double profileWeight = weights.getOrDefault(scenario.profile(), 0.0);
            long profileCount = demandVariants.stream().filter(v -> v.profile() == scenario.profile()).count();
            double itemWeight = profileCount == 0 ? 0.0 : profileWeight / profileCount;
            itemWeights.add(itemWeight);
            totalWeight += itemWeight;
        }
        double pick = random.nextDouble() * totalWeight;
        double running = 0.0;
        for (int i = 0; i < demandVariants.size(); i++) {
            running += itemWeights.get(i);
            if (pick <= running) {
                return demandVariants.get(i);
            }
        }
        return demandVariants.get(demandVariants.size() - 1);
    }

    private double trendFactor(LocalDate orderDate, boolean growing) {
        LocalDate startDate = properties.anchorDate().minusDays(properties.historyDays() - 1L);
        double progress = Math.max(0.0, Math.min(1.0,
                (double) java.time.temporal.ChronoUnit.DAYS.between(startDate, orderDate) / Math.max(1, properties.historyDays() - 1)));
        return growing ? 0.35 + progress * 1.65 : 2.0 - progress * 1.65;
    }

    private int determineQuantity(Random random, DemandProfile profile) {
        return switch (profile) {
            case SMOOTH -> 2 + random.nextInt(3);
            case NORMAL -> 1 + random.nextInt(3);
            case SLOW -> 1;
            case INTERMITTENT -> random.nextDouble() < 0.80 ? 1 : 2;
            case ERRATIC -> 1 + random.nextInt(8);
            case GROWING -> 1 + random.nextInt(4);
            case DECLINING -> 1 + random.nextInt(3);
            case NEW_ITEM -> 1 + random.nextInt(3);
            case NO_DEMAND -> 0;
        };
    }

    private void seedInventoryPoliciesAndScenarios(Random random, List<ScenarioVariant> scenarioVariants, Map<UUID, ScenarioTotals> totals) {
        Timestamp now = Timestamp.valueOf(properties.anchorDate().atStartOfDay());
        List<Object[]> policyBatch = new ArrayList<>();
        List<Object[]> scenarioBatch = new ArrayList<>();

        for (int i = 0; i < scenarioVariants.size(); i++) {
            ScenarioVariant scenario = scenarioVariants.get(i);
            ProductVariant variant = scenario.variant();
            ScenarioTotals scenarioTotals = totals.get(variant.getId());
            String supplierName = supplierName(i);
            int leadTime = properties.minLeadTimeDays()
                    + random.nextInt(properties.maxLeadTimeDays() - properties.minLeadTimeDays() + 1);
            int moq = 5 + (i % 6) * 5;
            int packSize = switch (i % 4) {
                case 0 -> 1;
                case 1 -> 5;
                case 2 -> 10;
                default -> 20;
            };
            BigDecimal serviceLevel = properties.effectiveDefaultServiceLevel()
                    .subtract(BigDecimal.valueOf((i % 3) * 0.01))
                    .setScale(3, RoundingMode.HALF_UP);

            policyBatch.add(new Object[]{
                    nextUuid(random), variant.getId(), leadTime, 30 + (i % 5) * 7, serviceLevel,
                    moq, packSize, supplierName, true, now, now
            });
            scenarioBatch.add(new Object[]{
                    nextUuid(random), variant.getId(), properties.marker(), properties.scenarioVersion(), properties.randomSeed(),
                    properties.anchorDate(), properties.historyDays(), scenario.profile().name(),
                    scenarioTotals.totalUnits, scenarioTotals.validUnits, supplierName, leadTime, moq, packSize, serviceLevel,
                    "{\"source\":\"synthetic-seeder-v2\"}", now, now
            });
        }

        jdbcTemplate.batchUpdate("""
            insert into inventory_policies (
                id, variant_id, lead_time_days, target_cover_days, service_level,
                minimum_order_quantity, pack_size, supplier_name, active, created_at, updated_at
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            on conflict (variant_id) do update set
                lead_time_days = excluded.lead_time_days,
                target_cover_days = excluded.target_cover_days,
                service_level = excluded.service_level,
                minimum_order_quantity = excluded.minimum_order_quantity,
                pack_size = excluded.pack_size,
                supplier_name = excluded.supplier_name,
                active = excluded.active,
                updated_at = excluded.updated_at
        """, policyBatch);

        if (properties.groundTruthEnabled()) {
            jdbcTemplate.batchUpdate("""
                insert into forecast_demo_scenarios (
                    id, variant_id, marker, scenario_version, random_seed, anchor_date, history_days,
                    demand_profile, expected_total_units, expected_valid_units, supplier_name, lead_time_days,
                    minimum_order_quantity, pack_size, service_level, metadata_json, created_at, updated_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?)
            """, scenarioBatch);
        }
    }

    private String supplierName(int index) {
        return "Demo Supplier " + ((index % properties.supplierCount()) + 1);
    }

    private UUID nextUuid(Random random) {
        return new UUID(random.nextLong(), random.nextLong());
    }

    private enum DemandProfile {
        SMOOTH(7.0), NORMAL(5.5), SLOW(2.0), INTERMITTENT(1.1), ERRATIC(2.6),
        GROWING(2.8), DECLINING(2.8), NEW_ITEM(1.8), NO_DEMAND(0.0);

        private final double baseWeight;

        DemandProfile(double baseWeight) {
            this.baseWeight = baseWeight;
        }

        private double baseWeight() {
            return baseWeight;
        }
    }

    private record ScenarioVariant(ProductVariant variant, DemandProfile profile) {}

    private static final class ScenarioTotals {
        private int totalUnits;
        private int validUnits;
    }
}