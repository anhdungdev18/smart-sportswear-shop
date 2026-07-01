package com.dunghaiquyen.ecommerce.modules.report.repository;

import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Report-only read queries against {@code product_variants} - see OrderReportRepository's javadoc for why this is separate. */
public interface ProductVariantReportRepository extends JpaRepository<ProductVariant, UUID> {

    @Query("select coalesce(sum(v.stockQuantity), 0) from ProductVariant v")
    long sumStockQuantity();

    @Query("select coalesce(sum(v.reservedQuantity), 0) from ProductVariant v")
    long sumReservedQuantity();

    /**
     * "Low stock" = available (stock - reserved) at or below the threshold,
     * excluding INACTIVE variants (a discontinued variant running low is not
     * useful information for restocking). PHASE1_SPEC.md never gives a
     * concrete number - see AppReportProperties/ReportService for the chosen
     * default and why.
     */
    @Query("select count(v) from ProductVariant v "
            + "where v.status <> :excludedStatus and (v.stockQuantity - v.reservedQuantity) <= :threshold")
    long countLowStock(@Param("excludedStatus") VariantStatus excludedStatus, @Param("threshold") int threshold);

    @Query("select v from ProductVariant v join fetch v.product "
            + "where v.status <> :excludedStatus and (v.stockQuantity - v.reservedQuantity) <= :threshold "
            + "order by (v.stockQuantity - v.reservedQuantity) asc")
    List<ProductVariant> findLowStockVariants(
            @Param("excludedStatus") VariantStatus excludedStatus, @Param("threshold") int threshold, Pageable pageable);
}
