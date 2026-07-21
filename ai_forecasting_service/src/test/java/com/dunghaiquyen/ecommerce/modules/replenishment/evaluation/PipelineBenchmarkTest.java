package com.dunghaiquyen.ecommerce.modules.replenishment.evaluation;

import static org.assertj.core.api.Assertions.assertThat;

import com.dunghaiquyen.ecommerce.modules.replenishment.dto.ForecastGenerationStatus;
import com.dunghaiquyen.ecommerce.modules.replenishment.service.ForecastGenerationService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
class PipelineBenchmarkTest {

    private static final Logger log = LoggerFactory.getLogger(PipelineBenchmarkTest.class);

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.jwt.access-secret", () -> "integration-test-access-secret-that-is-at-least-32-bytes");
        registry.add("app.jwt.refresh-secret", () -> "integration-test-access-secret-that-is-at-least-32-bytes-refresh");
        registry.add("app.forecast.generation-parallelism", () -> "4");
    }

    @Autowired org.springframework.jdbc.core.simple.JdbcClient jdbc;
    @Autowired ForecastGenerationService forecastGenerationService;

    @Test
    void benchmarkPipeline() throws Exception {
        log.info("Seeding 545 mock variants for benchmark...");
        List<UUID> variantIds = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Ensure tables exist and seed
        for (int i = 0; i < 2000; i++) {
            UUID variantId = UUID.randomUUID();
            variantIds.add(variantId);
            
            jdbc.sql("insert into inventory_policies (id, variant_id, lead_time_days, target_cover_days, service_level, minimum_order_quantity, pack_size, active, created_at, updated_at) " +
                     "values (:id, :v, 7, 30, 0.95, 10, 1, true, now(), now())")
                .param("id", UUID.randomUUID())
                .param("v", variantId)
                .update();
                
            jdbc.sql("insert into ai_product_variant_snapshot (variant_id, product_id, sku, product_name, size, color, captured_at) " +
                     "values (:v, :p, :sku, 'Mock Product', 'L', 'Red', now())")
                .param("v", variantId)
                .param("p", UUID.randomUUID())
                .param("sku", "SKU-" + i)
                .update();

            jdbc.sql("insert into ai_inventory_snapshot (variant_id, stock_quantity, reserved_quantity, captured_at) " +
                     "values (:v, 50, 0, now())")
                .param("v", variantId)
                .update();

            for (int d = 0; d < 180; d+=30) {
                // Just insert sparse demand to speed up seed but have some data
                jdbc.sql("insert into ai_sales_daily_snapshot (variant_id, sales_date, quantity, captured_at) " +
                         "values (:v, :sd, :q, now())")
                    .param("v", variantId)
                    .param("sd", java.sql.Date.valueOf(today.minusDays(d)))
                    .param("q", 5)
                    .update();
            }
        }
        
        log.info("Starting model evaluation phase...");
        forecastGenerationService.startSync();
        forecastGenerationService.startEvaluationAsync(variantIds, today.minusDays(180), today);
        
        while (forecastGenerationService.getStatus().status() != ForecastGenerationStatus.Status.COMPLETED &&
               forecastGenerationService.getStatus().status() != ForecastGenerationStatus.Status.FAILED) {
            Thread.sleep(500);
        }
        
        assertThat(forecastGenerationService.getStatus().status()).isEqualTo(ForecastGenerationStatus.Status.COMPLETED);
        log.info("Model evaluation finished in {} ms", forecastGenerationService.getStatus().durationMillis());

        log.info("Starting daily forecast generation phase...");
        forecastGenerationService.startSync();
        forecastGenerationService.startGenerationAsync(variantIds, today.minusDays(180), today);
        
        while (forecastGenerationService.getStatus().status() != ForecastGenerationStatus.Status.COMPLETED &&
               forecastGenerationService.getStatus().status() != ForecastGenerationStatus.Status.FAILED) {
            Thread.sleep(500);
        }

        assertThat(forecastGenerationService.getStatus().status()).isEqualTo(ForecastGenerationStatus.Status.COMPLETED);
        log.info("Daily forecast finished in {} ms", forecastGenerationService.getStatus().durationMillis());
    }
}
