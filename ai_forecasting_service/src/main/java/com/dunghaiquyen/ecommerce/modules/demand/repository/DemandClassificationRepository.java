package com.dunghaiquyen.ecommerce.modules.demand.repository;

import com.dunghaiquyen.ecommerce.modules.demand.dto.DemandClassificationResponse;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class DemandClassificationRepository {

    private final JdbcClient jdbcClient;

    public DemandClassificationRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<DemandHistory> findHistories(LocalDate fromInclusive, LocalDate toInclusive, String dataSource) {
        Map<VariantKey, List<DemandPoint>> grouped = new LinkedHashMap<>();
        jdbcClient.sql("""
                select p.variant_id,
                       p.sku,
                       p.product_name,
                       p.data_source,
                       s.sales_date,
                       coalesce(s.quantity, 0) quantity
                from ai_product_variant_snapshot p
                left join ai_sales_daily_snapshot s
                  on s.variant_id = p.variant_id
                 and s.data_source = p.data_source
                 and s.sales_date >= :fromDate
                 and s.sales_date <= :toDate
                where p.data_source = :dataSource
                order by p.sku, s.sales_date
                """)
                .param("fromDate", Date.valueOf(fromInclusive))
                .param("toDate", Date.valueOf(toInclusive))
                .param("dataSource", dataSource)
                .query((rs, rowNum) -> {
                    VariantKey key = new VariantKey(
                            rs.getObject("variant_id", UUID.class),
                            rs.getString("sku"),
                            rs.getString("product_name"),
                            rs.getString("data_source"));
                    grouped.computeIfAbsent(key, ignored -> new ArrayList<>());
                    Date salesDate = rs.getObject("sales_date", Date.class);
                    if (salesDate != null) {
                        grouped.get(key).add(new DemandPoint(salesDate.toLocalDate(), rs.getLong("quantity")));
                    }
                    return key;
                })
                .list();
        return grouped.entrySet().stream()
                .map(entry -> new DemandHistory(entry.getKey(), List.copyOf(entry.getValue())))
                .toList();
    }

    public List<DemandClassificationResponse> findSaved(String dataSource) {
        return jdbcClient.sql("""
                select variant_id, sku, product_name, data_source, from_date, to_date, history_days,
                       non_zero_days, total_units, adi, cv_squared, trend_slope, classification,
                       confidence, reason, algorithm_version
                from demand_classifications
                where data_source = :dataSource
                order by sku
                """)
                .param("dataSource", dataSource)
                .query(this::mapSaved)
                .list();
    }

    public Optional<DemandClassificationResponse> findSaved(UUID variantId, String dataSource) {
        return jdbcClient.sql("""
                select variant_id, sku, product_name, data_source, from_date, to_date, history_days,
                       non_zero_days, total_units, adi, cv_squared, trend_slope, classification,
                       confidence, reason, algorithm_version
                from demand_classifications
                where variant_id = :variantId and data_source = :dataSource
                """)
                .param("variantId", variantId)
                .param("dataSource", dataSource)
                .query(this::mapSaved)
                .optional();
    }

    public void upsertAll(List<DemandClassificationResponse> classifications) {
        for (DemandClassificationResponse row : classifications) {
            jdbcClient.sql("""
                    insert into demand_classifications (
                        variant_id, sku, product_name, data_source, from_date, to_date, history_days,
                        non_zero_days, total_units, adi, cv_squared, trend_slope, classification,
                        confidence, reason, algorithm_version, classified_at
                    ) values (
                        :variantId, :sku, :productName, :dataSource, :fromDate, :toDate, :historyDays,
                        :nonZeroDays, :totalUnits, :adi, :cvSquared, :trendSlope, :classification,
                        :confidence, :reason, :algorithmVersion, now()
                    )
                    on conflict (variant_id, data_source, algorithm_version) do update set
                        sku = excluded.sku,
                        product_name = excluded.product_name,
                        from_date = excluded.from_date,
                        to_date = excluded.to_date,
                        history_days = excluded.history_days,
                        non_zero_days = excluded.non_zero_days,
                        total_units = excluded.total_units,
                        adi = excluded.adi,
                        cv_squared = excluded.cv_squared,
                        trend_slope = excluded.trend_slope,
                        classification = excluded.classification,
                        confidence = excluded.confidence,
                        reason = excluded.reason,
                        classified_at = excluded.classified_at
                    """)
                    .param("variantId", row.variantId())
                    .param("sku", row.sku())
                    .param("productName", row.productName())
                    .param("dataSource", row.dataSource())
                    .param("fromDate", Date.valueOf(row.fromDate()))
                    .param("toDate", Date.valueOf(row.toDate()))
                    .param("historyDays", row.historyDays())
                    .param("nonZeroDays", row.nonZeroDays())
                    .param("totalUnits", row.totalUnits())
                    .param("adi", row.adi())
                    .param("cvSquared", row.cvSquared())
                    .param("trendSlope", row.trendSlope())
                    .param("classification", row.classification().name())
                    .param("confidence", row.confidence().name())
                    .param("reason", row.reason())
                    .param("algorithmVersion", row.algorithmVersion())
                    .update();
        }
    }

    private DemandClassificationResponse mapSaved(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new DemandClassificationResponse(
                rs.getObject("variant_id", UUID.class),
                rs.getString("sku"),
                rs.getString("product_name"),
                rs.getString("data_source"),
                rs.getObject("from_date", Date.class).toLocalDate(),
                rs.getObject("to_date", Date.class).toLocalDate(),
                rs.getInt("history_days"),
                rs.getInt("non_zero_days"),
                rs.getLong("total_units"),
                rs.getDouble("adi"),
                rs.getDouble("cv_squared"),
                rs.getDouble("trend_slope"),
                com.dunghaiquyen.ecommerce.modules.demand.dto.DemandPattern.valueOf(rs.getString("classification")),
                com.dunghaiquyen.ecommerce.modules.demand.dto.DemandConfidence.valueOf(rs.getString("confidence")),
                rs.getString("reason"),
                rs.getString("algorithm_version"));
    }

    public record VariantKey(UUID variantId, String sku, String productName, String dataSource) {
    }

    public record DemandPoint(LocalDate date, long quantity) {
    }

    public record DemandHistory(VariantKey variant, List<DemandPoint> points) {
    }
}
