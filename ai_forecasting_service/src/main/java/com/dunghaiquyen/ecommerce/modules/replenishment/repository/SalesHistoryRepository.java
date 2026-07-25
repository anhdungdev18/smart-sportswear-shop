package com.dunghaiquyen.ecommerce.modules.replenishment.repository;
import java.sql.Date;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
@Repository
public class SalesHistoryRepository {
    private final JdbcClient jdbcClient;
    public SalesHistoryRepository(JdbcClient jdbcClient) { this.jdbcClient = jdbcClient; }
    public List<DailyVariantDemandProjection> aggregateDailyDemand(Instant fromInclusive, Instant toExclusive, UUID[] variantIds, String dataSource) {
        var zone = ZoneId.of("Asia/Ho_Chi_Minh");
        return jdbcClient.sql("""
                select variant_id, sales_date demand_date, quantity
                from ai_sales_daily_snapshot
                where sales_date >= :fromDate and sales_date < :toDate and variant_id = any(:variantIds)
                  and data_source = :dataSource
                order by variant_id, sales_date
                """).param("fromDate", Date.valueOf(fromInclusive.atZone(zone).toLocalDate()))
                .param("toDate", Date.valueOf(toExclusive.atZone(zone).toLocalDate()))
                .param("variantIds", variantIds)
                .param("dataSource", dataSource)
                .query((rs, rowNum) -> (DailyVariantDemandProjection) new DemandRow(
                        rs.getObject("variant_id", UUID.class),
                        rs.getObject("demand_date", Date.class).toLocalDate(), rs.getLong("quantity"))).list();
    }
    private record DemandRow(UUID variantId, java.time.LocalDate demandDate, long quantity)
            implements DailyVariantDemandProjection {
        public UUID getVariantId() { return variantId; }
        public java.time.LocalDate getDemandDate() { return demandDate; }
        public long getQuantity() { return quantity; }
    }
}

