package com.dunghaiquyen.ecommerce.modules.replenishment.service;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
public class CoreSnapshotSyncService {
    private final JdbcTemplate jdbc;
    private final RestClient coreClient;
    private final String syncSecret;

    public CoreSnapshotSyncService(DataSource dataSource,
            @Value("${app.core.base-url}") String coreBaseUrl,
            @Value("${app.core.sync-secret}") String syncSecret) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.coreClient = RestClient.builder().baseUrl(coreBaseUrl).build();
        this.syncSecret = syncSecret;
    }

    @Transactional
    public SyncResult sync(LocalDate fromInclusive, LocalDate toInclusive, List<UUID> variantIds) {
        ApiResponse<SnapshotPayload> response = coreClient.post()
                .uri("/internal/v1/ai/replenishment/snapshot")
                .header("X-AI-Sync-Secret", syncSecret)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SnapshotRequest(fromInclusive, toInclusive, variantIds))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        if (response == null || !response.success() || response.data() == null) {
            throw new IllegalStateException("Core snapshot API returned no usable data");
        }
        SnapshotPayload payload = response.data();
        Timestamp capturedAt = Timestamp.from(payload.generatedAt());
        batchVariants(payload.variants(), capturedAt);
        batchDailyDemand(payload.dailyDemand(), capturedAt);
        batchSuppliers(payload.suppliers(), capturedAt);        return new SyncResult(payload.generatedAt(), payload.variants().size(),
                payload.dailyDemand().size(), payload.suppliers().size());
    }

    private void batchVariants(List<VariantData> variants, Timestamp capturedAt) {
        if (variants.isEmpty()) return;
        List<Object[]> products = new ArrayList<>(variants.size());
        List<Object[]> inventory = new ArrayList<>(variants.size());
        List<Object[]> policies = new ArrayList<>(variants.size());
        for (VariantData v : variants) {
            products.add(new Object[] {v.id(), v.productId(), v.sku(), v.productName(), v.size(), v.color(), capturedAt, v.dataSource()});
            inventory.add(new Object[] {v.id(), v.stockQuantity(), v.reservedQuantity(), capturedAt, v.dataSource()});
            policies.add(new Object[] {UUID.randomUUID(), v.id(), capturedAt, capturedAt});
        }
        jdbc.batchUpdate("""
                insert into ai_product_variant_snapshot
                    (variant_id, product_id, sku, product_name, size, color, captured_at, data_source)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (variant_id) do update set product_id=excluded.product_id, sku=excluded.sku,
                    product_name=excluded.product_name, size=excluded.size, color=excluded.color,
                    captured_at=excluded.captured_at, data_source=excluded.data_source
                """, products);
        jdbc.batchUpdate("""
                insert into ai_inventory_snapshot
                    (variant_id, stock_quantity, reserved_quantity, captured_at, data_source)
                values (?, ?, ?, ?, ?)
                on conflict (variant_id, captured_at) do update set
                    stock_quantity=excluded.stock_quantity, reserved_quantity=excluded.reserved_quantity, data_source=excluded.data_source
                """, inventory);
        jdbc.batchUpdate("""
                insert into inventory_policies
                    (id, variant_id, lead_time_days, target_cover_days, service_level,
                     minimum_order_quantity, pack_size, active, created_at, updated_at)
                values (?, ?, 7, 30, 0.950, 1, 1, true, ?, ?)
                on conflict (variant_id) do nothing
                """, policies);
    }

    private void batchDailyDemand(List<DemandData> demand, Timestamp capturedAt) {
        if (demand.isEmpty()) return;
        List<Object[]> rows = demand.stream()
                .map(d -> new Object[] {d.variantId(), d.demandDate(), d.quantity(), capturedAt, d.dataSource()})
                .toList();
        jdbc.batchUpdate("""
                insert into ai_sales_daily_snapshot (variant_id, sales_date, quantity, captured_at, data_source)
                values (?, ?, ?, ?, ?)
                on conflict (variant_id, sales_date, data_source) do update set quantity=excluded.quantity,
                    captured_at=excluded.captured_at, data_source=excluded.data_source
                """, rows);
    }

    private void batchSuppliers(List<SupplierData> suppliers, Timestamp capturedAt) {
        if (suppliers.isEmpty()) return;
        List<Object[]> rows = suppliers.stream()
                .map(s -> new Object[] {s.id(), s.code(), s.name(), s.active(), s.defaultLeadTimeDays(), capturedAt,
                        s.updatedAt() == null ? null : Timestamp.from(s.updatedAt())})
                .toList();
        jdbc.batchUpdate("""
                insert into ai_supplier_snapshot (supplier_id, supplier_code, supplier_name, active,
                    default_lead_time_days, captured_at, source_updated_at)
                values (?, ?, ?, ?, ?, ?, ?)
                on conflict (supplier_id) do update set supplier_code=excluded.supplier_code,
                    supplier_name=excluded.supplier_name, active=excluded.active,
                    default_lead_time_days=excluded.default_lead_time_days,
                    captured_at=excluded.captured_at, source_updated_at=excluded.source_updated_at
                """, rows);
    }
    public record SnapshotRequest(LocalDate fromInclusive, LocalDate toInclusive, List<UUID> variantIds) {}
    public record SnapshotPayload(Instant generatedAt, List<VariantData> variants,
                                  List<DemandData> dailyDemand, List<SupplierData> suppliers) {}
    public record VariantData(UUID id, UUID productId, String sku, String productName, String size,
                              String color, int stockQuantity, int reservedQuantity, String dataSource) {}
    public record DemandData(UUID variantId, LocalDate demandDate, long quantity, String dataSource) {}
    public record SupplierData(UUID id, String code, String name, boolean active,
                               Integer defaultLeadTimeDays, Instant updatedAt) {}
    public record SyncResult(Instant capturedAt, int variants, int dailySalesRows, int suppliers) {}
}




