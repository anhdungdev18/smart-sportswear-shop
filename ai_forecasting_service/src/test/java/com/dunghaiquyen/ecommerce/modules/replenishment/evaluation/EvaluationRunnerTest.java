package com.dunghaiquyen.ecommerce.modules.replenishment.evaluation;

import static org.assertj.core.api.Assertions.assertThat;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastAlgorithmType;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.InventoryPolicy;
import com.dunghaiquyen.ecommerce.modules.replenishment.forecasting.ForecastAlgorithm;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.InventoryPolicyRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.VariantReadRepository;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.VariantSnapshot;
import com.dunghaiquyen.ecommerce.modules.replenishment.service.DailyDemandService;
import com.dunghaiquyen.ecommerce.modules.replenishment.service.ForecastBacktestService;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class EvaluationRunnerTest {
    private static final Logger log = LoggerFactory.getLogger(EvaluationRunnerTest.class);

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.jwt.access-secret", () -> "integration-test-access-secret-that-is-at-least-32-bytes");
        registry.add("app.jwt.refresh-secret", () -> "integration-test-access-secret-that-is-at-least-32-bytes-refresh");
    }

    @Autowired DailyDemandService dailyDemandService;
    @Autowired ForecastBacktestService backtestService;
    @Autowired VariantReadRepository variantRepository;
    @Autowired InventoryPolicyRepository policyRepository;
    @Autowired List<ForecastAlgorithm> algorithms;

    static class SimulationResult {
        int initialStock;
        int stockoutDays;
        int unitsShort;
        int totalDemand;
        double sumOnHand;
        int totalOrders;
        int totalUnitsOrdered;

        public double getStockoutRate() {
            return 120 == 0 ? 0 : (double) stockoutDays / 120.0;
        }

        public double getFillRate() {
            return totalDemand == 0 ? 1.0 : (double) (totalDemand - unitsShort) / totalDemand;
        }

        public double getAverageOnHand() {
            return sumOnHand / 120.0;
        }
    }

    @Autowired org.springframework.jdbc.core.simple.JdbcClient jdbc;

    @Test
    void runEvaluationAndExportFiles() throws Exception {
        log.info("Starting Evaluation Runner...");

        seedMockDataIfEmpty();

        List<UUID> activeVariants = variantRepository.findAllActiveIds();
        log.info("Found {} active variants for evaluation.", activeVariants.size());

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(180);

        Map<UUID, List<DailyDemandService.DailyDemandPoint>> demandData = 
                dailyDemandService.getDailyDemand(activeVariants, startDate, endDate);

        List<VariantSnapshot> variants = variantRepository.findAllByIds(activeVariants);
        Map<UUID, VariantSnapshot> variantMap = new HashMap<>();
        variants.forEach(v -> variantMap.put(v.id(), v));

        List<InventoryPolicy> policies = policyRepository.findAllByVariantIdIn(activeVariants);
        Map<UUID, InventoryPolicy> policyMap = new HashMap<>();
        policies.forEach(p -> policyMap.put(p.getVariantId(), p));

        Path rootPath = Paths.get("target/evaluation_output");
        Files.createDirectories(rootPath);
        
        try (PrintWriter simWriter = new PrintWriter(Files.newBufferedWriter(rootPath.resolve("inventory_simulation.csv"), StandardCharsets.UTF_8));
             PrintWriter sumWriter = new PrintWriter(Files.newBufferedWriter(rootPath.resolve("summary.md"), StandardCharsets.UTF_8))) {
            
            simWriter.println("variant_id,sku,demand_profile,policy,initial_stock,lead_time_days,stockout_days,stockout_rate,units_short,fill_rate,average_on_hand,total_orders,total_units_ordered");

            long totalStockoutBaseline = 0, totalStockoutProposed = 0;
            long totalUnitsShortBaseline = 0, totalUnitsShortProposed = 0;
            long totalDemandGlobal = 0;
            double sumOnHandBaseline = 0, sumOnHandProposed = 0;
            int count = 0;

            for (UUID variantId : activeVariants) {
                List<DailyDemandService.DailyDemandPoint> points = demandData.get(variantId);
                if (points == null || points.size() < 180) continue;

                List<Integer> demand = points.stream().map(p -> (int) p.quantity()).toList();
                VariantSnapshot variant = variantMap.get(variantId);
                InventoryPolicy policy = policyMap.get(variantId);
                if (policy == null) continue;

                count++;

                SimulationResult baseline = simulate(demand, policy, true);
                SimulationResult proposed = simulate(demand, policy, false);

                totalStockoutBaseline += baseline.stockoutDays;
                totalStockoutProposed += proposed.stockoutDays;
                totalUnitsShortBaseline += baseline.unitsShort;
                totalUnitsShortProposed += proposed.unitsShort;
                totalDemandGlobal += baseline.totalDemand;
                sumOnHandBaseline += baseline.sumOnHand;
                sumOnHandProposed += proposed.sumOnHand;

                writeSimRow(simWriter, variant, "SEASONAL", "Baseline", policy, baseline);
                writeSimRow(simWriter, variant, "SEASONAL", "Proposed", policy, proposed);
            }

            sumWriter.println("# Evaluation Summary");
            sumWriter.println();
            sumWriter.println("| Metric | Baseline | Proposed | Chênh lệch |");
            sumWriter.println("|---|---:|---:|---:|");
            
            if (count > 0) {
                double avgStockoutDaysBase = (double) totalStockoutBaseline / count;
                double avgStockoutDaysProp = (double) totalStockoutProposed / count;
                
                double avgUnitsShortBase = (double) totalUnitsShortBaseline / count;
                double avgUnitsShortProp = (double) totalUnitsShortProposed / count;

                double avgOnHandBase = sumOnHandBaseline / (count * 120.0);
                double avgOnHandProp = sumOnHandProposed / (count * 120.0);

                double fillRateBase = totalDemandGlobal == 0 ? 1 : 1 - ((double) totalUnitsShortBaseline / totalDemandGlobal);
                double fillRateProp = totalDemandGlobal == 0 ? 1 : 1 - ((double) totalUnitsShortProposed / totalDemandGlobal);

                sumWriter.printf("| Stockout days (avg) | %.2f | %.2f | %.2f |%n", avgStockoutDaysBase, avgStockoutDaysProp, avgStockoutDaysProp - avgStockoutDaysBase);
                sumWriter.printf("| Units short (avg) | %.2f | %.2f | %.2f |%n", avgUnitsShortBase, avgUnitsShortProp, avgUnitsShortProp - avgUnitsShortBase);
                sumWriter.printf("| Fill rate | %.2f%% | %.2f%% | %.2f%% |%n", fillRateBase * 100, fillRateProp * 100, (fillRateProp - fillRateBase) * 100);
                sumWriter.printf("| Average on-hand | %.2f | %.2f | %.2f |%n", avgOnHandBase, avgOnHandProp, avgOnHandProp - avgOnHandBase);
            } else {
                sumWriter.println("| No data | | | |");
            }
        }

        log.info("Evaluation completed. Files written to {}", rootPath.toAbsolutePath());
        assertThat(rootPath.resolve("inventory_simulation.csv")).exists();
        assertThat(rootPath.resolve("summary.md")).exists();
    }

    private SimulationResult simulate(List<Integer> fullDemand, InventoryPolicy policy, boolean isBaseline) {
        SimulationResult result = new SimulationResult();
        result.initialStock = 50;
        int stock = result.initialStock;
        
        Map<Integer, Integer> arrivals = new HashMap<>();

        for (int t = 60; t < 180; t++) {
            List<Integer> window = fullDemand.subList(t - 30, t);
            
            double dailyForecast = 0;
            if (isBaseline) {
                ForecastAlgorithm ma = algorithms.stream().filter(a -> a.type() == ForecastAlgorithmType.MOVING_AVERAGE).findFirst().orElseThrow();
                dailyForecast = ma.forecast(window, policy.getLeadTimeDays() + policy.getTargetCoverDays()).averageDailyDemand();
            } else {
                var backtest = backtestService.runBacktest(window, 30);
                ForecastAlgorithmType best = backtest.bestAlgorithm() != null ? backtest.bestAlgorithm() : ForecastAlgorithmType.MOVING_AVERAGE;
                ForecastAlgorithm algo = algorithms.stream().filter(a -> a.type() == best).findFirst().orElseThrow();
                dailyForecast = algo.forecast(window, policy.getLeadTimeDays() + policy.getTargetCoverDays()).averageDailyDemand();
            }

            int arrived = arrivals.getOrDefault(t, 0);
            stock += arrived;

            int demandToday = fullDemand.get(t);
            result.totalDemand += demandToday;

            if (demandToday > stock) {
                result.unitsShort += (demandToday - stock);
                result.stockoutDays++;
                stock = 0;
            } else {
                stock -= demandToday;
            }

            result.sumOnHand += stock;

            int reorderPoint = (int) Math.ceil(policy.getLeadTimeDays() * dailyForecast);
            final int currentT = t;
            int incoming = arrivals.entrySet().stream().filter(e -> e.getKey() > currentT).mapToInt(Map.Entry::getValue).sum();

            if (stock + incoming < reorderPoint) {
                int orderQty = (int) Math.ceil(policy.getTargetCoverDays() * dailyForecast);
                orderQty = Math.max(orderQty, policy.getMinimumOrderQuantity());
                
                if (policy.getPackSize() > 1) {
                    orderQty = (int) Math.ceil((double) orderQty / policy.getPackSize()) * policy.getPackSize();
                }

                arrivals.put(t + policy.getLeadTimeDays(), arrivals.getOrDefault(t + policy.getLeadTimeDays(), 0) + orderQty);
                result.totalOrders++;
                result.totalUnitsOrdered += orderQty;
            }
        }
        return result;
    }

    private void writeSimRow(PrintWriter writer, VariantSnapshot variant, String profile, String policy, InventoryPolicy invPolicy, SimulationResult res) {
        writer.printf("%s,%s,%s,%s,%d,%d,%d,%.4f,%d,%.4f,%.2f,%d,%d%n",
                variant.id(), variant.sku(), profile, policy,
                res.initialStock, invPolicy.getLeadTimeDays(),
                res.stockoutDays, res.getStockoutRate(),
                res.unitsShort, res.getFillRate(),
                res.getAverageOnHand(), res.totalOrders, res.totalUnitsOrdered);
    }

    private void seedMockDataIfEmpty() {
        Integer count = jdbc.sql("select count(*) from inventory_policies").query(Integer.class).single();
        if (count != null && count > 0) return;

        log.info("Seeding mock data for Evaluation Runner...");
        UUID variantId = UUID.randomUUID();
        jdbc.sql("insert into inventory_policies (id, variant_id, lead_time_days, target_cover_days, service_level, minimum_order_quantity, pack_size, active, created_at, updated_at) " +
                 "values (:id, :v, 7, 30, 0.95, 10, 1, true, now(), now())")
            .param("id", UUID.randomUUID())
            .param("v", variantId)
            .update();
            
        jdbc.sql("insert into ai_product_variant_snapshot (variant_id, product_id, sku, product_name, size, color, captured_at) " +
                 "values (:v, :p, 'MOCK-SKU-1', 'Mock Product', 'L', 'Red', now())")
            .param("v", variantId)
            .param("p", UUID.randomUUID())
            .update();

        jdbc.sql("insert into ai_inventory_snapshot (variant_id, stock_quantity, reserved_quantity, captured_at) " +
                 "values (:v, 50, 0, now())")
            .param("v", variantId)
            .update();

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 180; i++) {
            LocalDate d = today.minusDays(180 - i);
            long qty = (long) (Math.random() * 5) + 1; // 1 to 5
            jdbc.sql("insert into ai_sales_daily_snapshot (variant_id, sales_date, quantity, captured_at) " +
                     "values (:v, :sd, :q, now())")
                .param("v", variantId)
                .param("sd", java.sql.Date.valueOf(d))
                .param("q", qty)
                .update();
        }
    }
}