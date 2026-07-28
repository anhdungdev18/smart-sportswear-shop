package com.dunghaiquyen.ecommerce.modules.replenishment.repository;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastModelEvaluation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForecastModelEvaluationRepository extends JpaRepository<ForecastModelEvaluation, UUID> {
    List<ForecastModelEvaluation> findAllByVariantIdIn(Iterable<UUID> variantIds);

    List<ForecastModelEvaluation> findAllByVariantIdInAndDataSource(Iterable<UUID> variantIds, String dataSource);
}
