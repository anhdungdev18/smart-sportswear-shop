package com.dunghaiquyen.ecommerce.modules.replenishment.repository;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastRun;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ForecastRunRepository extends JpaRepository<ForecastRun, UUID> {

    List<ForecastRun> findAllByVariantIdOrderByGeneratedAtDesc(UUID variantId);

    Optional<ForecastRun> findFirstByVariantIdOrderByGeneratedAtDesc(UUID variantId);

    Optional<ForecastRun> findFirstByVariantIdAndDataSourceOrderByGeneratedAtDesc(UUID variantId, String dataSource);

    @Query("""
            select f from ForecastRun f
            where f.variantId in :variantIds
              and f.dataSource = :dataSource
              and f.generatedAt = (
                  select max(latest.generatedAt) from ForecastRun latest
                  where latest.variantId = f.variantId and latest.dataSource = f.dataSource
              )
            """)
    List<ForecastRun> findLatestByVariantIdsAndDataSource(
            @Param("variantIds") List<UUID> variantIds,
            @Param("dataSource") String dataSource);

    void deleteAllByVariantIdIn(List<UUID> variantIds);

    void deleteAllByVariantIdInAndDataSource(List<UUID> variantIds, String dataSource);
}
