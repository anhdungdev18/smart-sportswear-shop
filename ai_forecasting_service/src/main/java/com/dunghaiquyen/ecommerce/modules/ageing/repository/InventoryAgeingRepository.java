package com.dunghaiquyen.ecommerce.modules.ageing.repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryAgeingRepository {
    private final JdbcClient jdbcClient;

    public InventoryAgeingRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<AgeingRow> findRows(String dataSource, LocalDate today) {
        return jdbcClient.sql("""
                with latest_inventory as (
                    select distinct on (variant_id, data_source)
                           variant_id, data_source, stock_quantity, reserved_quantity
                    from ai_inventory_snapshot
                    where data_source = :dataSource
                    order by variant_id, data_source, captured_at desc
                ), sales as (
                    select variant_id, data_source,
                           max(sales_date) filter (where quantity > 0) last_sale_date,
                           coalesce(sum(quantity) filter (where sales_date >= :day30), 0) sold_30,
                           coalesce(sum(quantity) filter (where sales_date >= :day90), 0) sold_90,
                           coalesce(sum(quantity) filter (where sales_date >= :day180), 0) sold_180
                    from ai_sales_daily_snapshot
                    where data_source = :dataSource
                    group by variant_id, data_source
                ), imports as (
                    select variant_id, max(created_at)::date last_import_date
                    from inventory_transactions
                    where type = 'IMPORT'
                    group by variant_id
                )
                select p.variant_id, p.product_id, p.sku, p.product_name, p.size, p.color,
                       greatest(0, coalesce(i.stock_quantity, 0) - coalesce(i.reserved_quantity, 0)) available_quantity,
                       coalesce(v.price, 0) unit_price,
                       coalesce(im.last_import_date, v.created_at::date, p.captured_at::date) stock_start_date,
                       s.last_sale_date, coalesce(s.sold_30, 0) sold_30,
                       coalesce(s.sold_90, 0) sold_90, coalesce(s.sold_180, 0) sold_180,
                       (ip.supplier_name is not null and btrim(ip.supplier_name) <> '') supplier_configured
                from ai_product_variant_snapshot p
                left join latest_inventory i on i.variant_id = p.variant_id and i.data_source = p.data_source
                left join sales s on s.variant_id = p.variant_id and s.data_source = p.data_source
                left join product_variants v on v.id = p.variant_id
                left join imports im on im.variant_id = p.variant_id
                left join inventory_policies ip on ip.variant_id = p.variant_id and ip.active = true
                where p.data_source = :dataSource
                order by available_quantity desc, p.sku
                """)
                .param("dataSource", dataSource)
                .param("day30", Date.valueOf(today.minusDays(29)))
                .param("day90", Date.valueOf(today.minusDays(89)))
                .param("day180", Date.valueOf(today.minusDays(179)))
                .query((rs, rowNum) -> new AgeingRow(
                        rs.getObject("variant_id", UUID.class), rs.getObject("product_id", UUID.class),
                        rs.getString("sku"), rs.getString("product_name"), rs.getString("size"), rs.getString("color"),
                        rs.getInt("available_quantity"), rs.getBigDecimal("unit_price"),
                        rs.getObject("stock_start_date", Date.class).toLocalDate(),
                        rs.getObject("last_sale_date", Date.class) == null ? null : rs.getObject("last_sale_date", Date.class).toLocalDate(),
                        rs.getLong("sold_30"), rs.getLong("sold_90"), rs.getLong("sold_180"),
                        rs.getBoolean("supplier_configured")))
                .list();
    }

    public record AgeingRow(UUID variantId, UUID productId, String sku, String productName, String size, String color,
                            int availableQuantity, BigDecimal unitPrice, LocalDate stockStartDate, LocalDate lastSaleDate,
                            long sold30, long sold90, long sold180, boolean supplierConfigured) {}
}
