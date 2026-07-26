package com.dunghaiquyen.ecommerce.modules.replenishment.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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

    /** Controls which fixture the mock server returns — avoids reading the HTTP request body. */
    private static final AtomicBoolean RETURN_GAP_FIXTURE = new AtomicBoolean(false);

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
            // Read and discard body to avoid socket issues with HttpServer
            exchange.getRequestBody().readAllBytes();
            byte[] response = (RETURN_GAP_FIXTURE.get() ? snapshotResponseWithGap() : snapshotResponse())
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        coreServer.start();
        service = new CoreSnapshotSyncService(
                dataSource, "http://127.0.0.1:" + coreServer.getAddress().getPort(), "integration-secret");
    }

    @BeforeEach
    void resetState() {
        // Truncate the sales snapshot so each test starts with a clean slate.
        // Other tables (product_variant_snapshot, inventory_snapshot, inventory_policies)
        // are intentionally kept to verify upsert / on-conflict-do-nothing behaviour.
        jdbc.sql("truncate table ai_sales_daily_snapshot").update();
        RETURN_GAP_FIXTURE.set(false);
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
        // Range is Jan 1–2 (2 days) and both days have demand rows → no zeros needed
        assertThat(first.materializedZeroRows()).isEqualTo(0);
        assertThat(count("ai_product_variant_snapshot")).isEqualTo(1);
        assertThat(count("ai_inventory_snapshot")).isGreaterThanOrEqualTo(1);
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

    @Test
    void syncMaterializesZeroDemandDaysForGaps() {
        RETURN_GAP_FIXTURE.set(true);
        // Range Jan 1–3 (3 days) but demand only on Jan 1 and Jan 3 → Jan 2 must be filled with 0
        var result = service.sync(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 3), List.of());
        assertThat(result.dailySalesRows()).isEqualTo(2); // 2 observed demand rows
        assertThat(result.materializedZeroRows()).isEqualTo(1); // 1 zero-demand row for the gap

        long totalRows = jdbc.sql("""
                select count(*) from ai_sales_daily_snapshot
                where variant_id = '11111111-1111-1111-1111-111111111111'
                  and sales_date >= '2026-02-01' and sales_date <= '2026-02-03'
                  and data_source = 'DEMO'
                """).query(Long.class).single();
        assertThat(totalRows).isEqualTo(3); // All 3 days present

        long zeroDayCount = jdbc.sql("""
                select count(*) from ai_sales_daily_snapshot
                where variant_id = '11111111-1111-1111-1111-111111111111'
                  and sales_date = '2026-02-02'
                  and quantity = 0
                  and data_source = 'DEMO'
                """).query(Long.class).single();
        assertThat(zeroDayCount).isEqualTo(1); // Feb 2 filled with quantity=0
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

    /** Fixture with a demand gap on Feb 2 to verify zero-demand materialization. */
    private static String snapshotResponseWithGap() {
        return """
                {"success":true,"message":"OK","data":{
                  "generatedAt":"2026-02-04T00:00:00Z",
                  "variants":[{"id":"11111111-1111-1111-1111-111111111111",
                    "productId":"22222222-2222-2222-2222-222222222222","sku":"FD-TEST-01",
                    "productName":"Áo chạy bộ","size":"M","color":"Đen",
                    "stockQuantity":20,"reservedQuantity":3,"dataSource":"DEMO"}],
                  "dailyDemand":[
                    {"variantId":"11111111-1111-1111-1111-111111111111","demandDate":"2026-02-01","quantity":3,"dataSource":"DEMO"},
                    {"variantId":"11111111-1111-1111-1111-111111111111","demandDate":"2026-02-03","quantity":5,"dataSource":"DEMO"}],
                  "suppliers":[]}}
                """;
    }
}
