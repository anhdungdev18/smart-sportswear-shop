package com.dunghaiquyen.ecommerce.modules.replenishment.repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
@Repository
public class VariantReadRepository {
    private final JdbcClient jdbcClient;
    public VariantReadRepository(JdbcClient jdbcClient) { this.jdbcClient = jdbcClient; }
    public Optional<VariantSnapshot> findById(UUID id) {
        return jdbcClient.sql("""
                select p.variant_id id, p.product_id, p.sku, p.product_name, p.size, p.color,
                       i.stock_quantity, i.reserved_quantity
                from ai_product_variant_snapshot p
                join lateral (select stock_quantity, reserved_quantity from ai_inventory_snapshot
                    where variant_id=p.variant_id order by captured_at desc limit 1) i on true
                where p.variant_id=:id
                """).param("id", id).query(VariantSnapshot.class).optional();
    }
    public List<UUID> findAllActiveIds() {
        return jdbcClient.sql("select variant_id from ai_product_variant_snapshot order by variant_id")
                .query(UUID.class).list();
    }
}
