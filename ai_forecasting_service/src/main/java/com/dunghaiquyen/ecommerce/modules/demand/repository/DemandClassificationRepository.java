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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Repository;

@Repository
public class DemandClassificationRepository {

    private final JdbcClient jdbcClient;
    private final JdbcTemplate jdbcTemplate;

    public DemandClassificationRepository(JdbcClient jdbcClient, JdbcTemplate jdbcTemplate) {
        this.jdbcClient = jdbcClient;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DemandHistory> findHistories(LocalDate fromInclusive, LocalDate toInclusive, String dataSource) {
        Map<VariantKey, List<DemandPoint>> grouped = new LinkedHashMap<>();
        jdbcClient.sql("""
                with coverage as (
                    select variant_id, data_source, count(*)::int observed_days,
                           bool_or(quantity > 0) has_demand
                    from ai_sales_daily_snapshot
                    where sales_date >= :fromDate and sales_date <= :toDate
                      and data_source = :dataSource
                    group by variant_id, data_source
                )
                select p.variant_id,
                       p.sku,
                       p.product_name,
                       p.data_source,
                       coalesce(c.observed_days, 0) observed_days,
                       s.sales_date,
                       coalesce(s.quantity, 0) quantity
                from ai_product_variant_snapshot p
                left join coverage c on c.variant_id = p.variant_id and c.data_source = p.data_source
                left join ai_sales_daily_snapshot s
                  on s.variant_id = p.variant_id
                 and s.data_source = p.data_source
                 and s.sales_date >= :fromDate
                 and s.sales_date <= :toDate
                 and coalesce(c.has_demand, false)
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
                            rs.getString("data_source"),
                            rs.getInt("observed_days"));
                    grouped.computeIfAbsent(key, ignored -> new ArrayList<>());
                    Date salesDate = rs.getObject("sales_date", Date.class);
                    if (salesDate != null) {
                        grouped.get(key).add(new DemandPoint(salesDate.toLocalDate(), rs.getLong("quantity")));
                    }
                    return key;
                })
                .list();
        return grouped.entrySet().stream()
                .map(entry -> new DemandHistory(entry.getKey(), List.copyOf(entry.getValue()), entry.getKey().observedDays()))
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
        if (classifications == null || classifications.isEmpty()) return;
        jdbcTemplate.batchUpdate("""
                    insert into demand_classifications (
                        variant_id, sku, product_name, data_source, from_date, to_date, history_days,
                        non_zero_days, total_units, adi, cv_squared, trend_slope, classification,
                        confidence, reason, algorithm_version, classified_at
                    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())
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
                    """, new BatchPreparedStatementSetter() {
            @Override public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                DemandClassificationResponse row = classifications.get(i);
                ps.setObject(1, row.variantId()); ps.setString(2, row.sku()); ps.setString(3, row.productName());
                ps.setString(4, row.dataSource()); ps.setDate(5, Date.valueOf(row.fromDate())); ps.setDate(6, Date.valueOf(row.toDate()));
                ps.setInt(7, row.historyDays()); ps.setInt(8, row.nonZeroDays()); ps.setLong(9, row.totalUnits());
                ps.setDouble(10, row.adi()); ps.setDouble(11, row.cvSquared()); ps.setDouble(12, row.trendSlope());
                ps.setString(13, row.classification().name()); ps.setString(14, row.confidence().name());
                ps.setString(15, row.reason()); ps.setString(16, row.algorithmVersion());
            }
            @Override public int getBatchSize() { return classifications.size(); }
        });
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

    public record VariantKey(UUID variantId, String sku, String productName, String dataSource, int observedDays) {
    }

    public record DemandPoint(LocalDate date, long quantity) {
    }

    public record DemandHistory(VariantKey variant, List<DemandPoint> points, int observedDays) {
    }
}
