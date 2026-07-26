package com.dunghaiquyen.ecommerce.modules.report.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/ai/replenishment")
public class AiReplenishmentDataController {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final JdbcClient jdbc;
    private final String syncSecret;
    private final String forecastDemoMarker;

    public AiReplenishmentDataController(JdbcClient jdbc,
            @Value("${app.ai.sync-secret}") String syncSecret,
            @Value("${app.forecast-demo.marker:[FORECAST_DEMO_V2]}") String forecastDemoMarker) {
        this.jdbc = jdbc;
        this.syncSecret = syncSecret;
        this.forecastDemoMarker = forecastDemoMarker;
    }

    @PostMapping("/snapshot")
    public ApiResponse<SnapshotResponse> snapshot(
            @RequestHeader("X-AI-Sync-Secret") String suppliedSecret,
            @RequestBody SnapshotRequest request) {
        if (syncSecret.isBlank() || !MessageDigest.isEqual(syncSecret.getBytes(StandardCharsets.UTF_8),
                suppliedSecret.getBytes(StandardCharsets.UTF_8))) {
            throw new AccessDeniedException("Invalid AI sync secret");
        }
        if (request.fromInclusive() == null || request.toInclusive() == null
                || request.fromInclusive().isAfter(request.toInclusive())) {
            throw new IllegalArgumentException("Invalid demand date range");
        }
        UUID[] ids = request.variantIds() == null ? new UUID[0]
                : request.variantIds().stream().distinct().toArray(UUID[]::new);
        boolean allActive = ids.length == 0;
        List<VariantData> variants = jdbc.sql("""
                select v.id, v.product_id, v.sku, p.name product_name, v.size, v.color,
                       v.stock_quantity, v.reserved_quantity,
                       case when fds.variant_id is not null then 'DEMO' else 'REAL' end data_source
                from product_variants v
                join products p on p.id = v.product_id
                left join forecast_demo_scenarios fds on fds.variant_id = v.id and fds.marker = :demoMarker
                where (:allActive and v.status = 'ACTIVE') or v.id = any(:ids)
                order by v.id
                """).param("demoMarker", forecastDemoMarker)
                .param("allActive", allActive)
                .param("ids", ids)
                .query(VariantData.class)
                .list();
        UUID[] selectedIds = variants.stream().map(VariantData::id).toArray(UUID[]::new);
        List<DemandData> demand = selectedIds.length == 0 ? List.of() : jdbc.sql("""
                select oi.variant_id, (o.created_at at time zone 'Asia/Ho_Chi_Minh')::date demand_date,
                       coalesce(o.data_source, 'REAL') data_source, sum(oi.quantity) quantity
                from order_items oi join orders o on o.id = oi.order_id
                where o.order_status <> 'CANCELLED'
                  and o.created_at >= :fromInclusive and o.created_at < :toExclusive
                  and oi.variant_id = any(:ids)
                group by oi.variant_id, demand_date, data_source
                order by oi.variant_id, demand_date, data_source
                """).param("fromInclusive", Timestamp.from(atStart(request.fromInclusive())))
                .param("toExclusive", Timestamp.from(atStart(request.toInclusive().plusDays(1))))
                .param("ids", selectedIds)
                .query((rs, row) -> new DemandData(rs.getObject("variant_id", UUID.class),
                        rs.getObject("demand_date", Date.class).toLocalDate(), rs.getLong("quantity"),
                        rs.getString("data_source")))
                .list();

        // Read distinct suppliers from inventory_policies for the selected variants.
        // supplier_name is used as both code and display name; a stable UUID is derived from it.
        List<SupplierData> suppliers = selectedIds.length == 0 ? List.of() : jdbc.sql("""
                select supplier_name,
                       cast(round(avg(lead_time_days)) as int) avg_lead_time_days
                from inventory_policies
                where variant_id = any(:ids)
                  and supplier_name is not null
                  and supplier_name <> ''
                  and active = true
                group by supplier_name
                order by supplier_name
                """).param("ids", selectedIds)
                .query((rs, row) -> {
                    String name = rs.getString("supplier_name");
                    int leadTime = rs.getInt("avg_lead_time_days");
                    UUID supplierId = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
                    return new SupplierData(supplierId, name, name, true, leadTime, null);
                })
                .list();

        return ApiResponse.ok(new SnapshotResponse(Instant.now(), variants, demand, suppliers));
    }

    private Instant atStart(LocalDate date) { return date.atStartOfDay(BUSINESS_ZONE).toInstant(); }
    public record SnapshotRequest(LocalDate fromInclusive, LocalDate toInclusive, List<UUID> variantIds) {}
    public record SnapshotResponse(Instant generatedAt, List<VariantData> variants,
                                   List<DemandData> dailyDemand, List<SupplierData> suppliers) {}
    public record VariantData(UUID id, UUID productId, String sku, String productName, String size,
                              String color, int stockQuantity, int reservedQuantity, String dataSource) {}
    public record DemandData(UUID variantId, LocalDate demandDate, long quantity, String dataSource) {}
    public record SupplierData(UUID id, String code, String name, boolean active,
                               Integer defaultLeadTimeDays, Instant updatedAt) {}
}
