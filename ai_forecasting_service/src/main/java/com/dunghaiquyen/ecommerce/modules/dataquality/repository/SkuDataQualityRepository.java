package com.dunghaiquyen.ecommerce.modules.dataquality.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class SkuDataQualityRepository {

    private final JdbcClient jdbcClient;

    public SkuDataQualityRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<SkuDataQualityRow> findQualityRows(LocalDate fromInclusive, LocalDate toInclusive) {
        return jdbcClient.sql(qualitySql() + " order by p.sku")
                .param("fromDate", Date.valueOf(fromInclusive))
                .param("toDate", Date.valueOf(toInclusive))
                .query((rs, rowNum) -> new SkuDataQualityRow(
                        rs.getObject("variant_id", UUID.class),
                        rs.getString("sku"),
                        rs.getString("product_name"),
                        rs.getInt("sales_rows"),
                        rs.getInt("non_zero_days"),
                        rs.getLong("total_units"),
                        rs.getObject("last_sale_date", Date.class) == null
                                ? null
                                : rs.getObject("last_sale_date", Date.class).toLocalDate(),
                        rs.getInt("inventory_snapshot_days"),
                        rs.getString("supplier_name")))
                .list();
    }

    public Optional<SkuDataQualityRow> findQualityRow(UUID variantId, LocalDate fromInclusive, LocalDate toInclusive) {
        return jdbcClient.sql(qualitySql() + " where p.variant_id = :variantId")
                .param("fromDate", Date.valueOf(fromInclusive))
                .param("toDate", Date.valueOf(toInclusive))
                .param("variantId", variantId)
                .query((rs, rowNum) -> new SkuDataQualityRow(
                        rs.getObject("variant_id", UUID.class),
                        rs.getString("sku"),
                        rs.getString("product_name"),
                        rs.getInt("sales_rows"),
                        rs.getInt("non_zero_days"),
                        rs.getLong("total_units"),
                        rs.getObject("last_sale_date", Date.class) == null
                                ? null
                                : rs.getObject("last_sale_date", Date.class).toLocalDate(),
                        rs.getInt("inventory_snapshot_days"),
                        rs.getString("supplier_name")))
                .optional();
    }

    private String qualitySql() {
        return """
                select p.variant_id,
                       p.sku,
                       p.product_name,
                       coalesce(s.sales_rows, 0) sales_rows,
                       coalesce(s.non_zero_days, 0) non_zero_days,
                       coalesce(s.total_units, 0) total_units,
                       s.last_sale_date,
                       coalesce(i.inventory_snapshot_days, 0) inventory_snapshot_days,
                       ip.supplier_name
                from ai_product_variant_snapshot p
                left join (
                    select variant_id,
                           count(*)::int sales_rows,
                           count(*) filter (where quantity > 0)::int non_zero_days,
                           coalesce(sum(quantity), 0) total_units,
                           max(sales_date) filter (where quantity > 0) last_sale_date
                    from ai_sales_daily_snapshot
                    where sales_date >= :fromDate and sales_date <= :toDate
                    group by variant_id
                ) s on s.variant_id = p.variant_id
                left join (
                    select variant_id,
                           count(distinct captured_at::date)::int inventory_snapshot_days
                    from ai_inventory_snapshot
                    where captured_at::date >= :fromDate and captured_at::date <= :toDate
                    group by variant_id
                ) i on i.variant_id = p.variant_id
                left join inventory_policies ip on ip.variant_id = p.variant_id and ip.active = true
                """;
    }
}
