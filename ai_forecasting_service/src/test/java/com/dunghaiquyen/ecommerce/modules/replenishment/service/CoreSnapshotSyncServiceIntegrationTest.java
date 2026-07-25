package com.dunghaiquyen.ecommerce.modules.replenishment.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class CoreSnapshotSyncServiceIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private static HttpServer coreServer;
    private static JdbcClient jdbc;
    private static CoreSnapshotSyncService service;

    @BeforeAll
    static void setUp() throws Exception {
        DataSource dataSource = new DriverManagerDataSource(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        Flyway.configure().dataSource(dataSource).locations("classpath:db/ai-migration")
                .table("flyway_ai_schema_history").load().migrate();
        jdbc = JdbcClient.create(dataSource);
        coreServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        coreServer.createContext("/internal/v1/ai/replenishment/snapshot", exchange -> {
            assertThat(exchange.getRequestHeaders().getFirst("X-AI-Sync-Secret"))
                    .isEqualTo("integration-secret");
            byte[] response = snapshotResponse().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        coreServer.start();
        service = new CoreSnapshotSyncService(
                dataSource, "http://127.0.0.1:" + coreServer.getAddress().getPort(), "integration-secret");
    }

    @AfterAll
    static void tearDown() {
        if (coreServer != null) coreServer.stop(0);
    }

    @Test
    void syncCreatesSnapshotsAndPreservesAdminPolicy() {
        var first = service.sync(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), List.of());
        assertThat(first.variants()).isEqualTo(1);
        assertThat(first.dailySalesRows()).isEqualTo(2);
        assertThat(count("ai_product_variant_snapshot")).isEqualTo(1);
        assertThat(count("ai_inventory_snapshot")).isEqualTo(1);
        assertThat(count("ai_sales_daily_snapshot")).isEqualTo(2);
        assertThat(jdbc.sql("select count(*) from ai_sales_daily_snapshot where data_source='DEMO'")
                .query(Long.class).single()).isEqualTo(2);
        assertThat(count("inventory_policies")).isEqualTo(1);

        UUID variantId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        jdbc.sql("update inventory_policies set lead_time_days=21 where variant_id=:variantId")
                .param("variantId", variantId).update();
        service.sync(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), List.of());
        assertThat(jdbc.sql("select lead_time_days from inventory_policies where variant_id=:variantId")
                .param("variantId", variantId).query(Integer.class).single()).isEqualTo(21);
        assertThat(count("inventory_policies")).isEqualTo(1);
    }

    private static long count(String table) {
        return jdbc.sql("select count(*) from " + table).query(Long.class).single();
    }

    private static String snapshotResponse() {
        return """
                {"success":true,"message":"OK","data":{
                  "generatedAt":"2026-01-03T00:00:00Z",
                  "variants":[{"id":"11111111-1111-1111-1111-111111111111",
                    "productId":"22222222-2222-2222-2222-222222222222","sku":"FD-TEST-01",
                    "productName":"Áo chạy bộ","size":"M","color":"Đen",
                    "stockQuantity":20,"reservedQuantity":3,"dataSource":"DEMO"}],
                  "dailyDemand":[
                    {"variantId":"11111111-1111-1111-1111-111111111111","demandDate":"2026-01-01","quantity":2,"dataSource":"DEMO"},
                    {"variantId":"11111111-1111-1111-1111-111111111111","demandDate":"2026-01-02","quantity":4,"dataSource":"DEMO"}],
                  "suppliers":[]}}
                """;
    }
}

