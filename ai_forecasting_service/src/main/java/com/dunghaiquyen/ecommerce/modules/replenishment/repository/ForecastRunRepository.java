package com.dunghaiquyen.ecommerce.modules.replenishment.repository;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastRun;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForecastRunRepository extends JpaRepository<ForecastRun, UUID> {

    List<ForecastRun> findAllByVariantIdOrderByGeneratedAtDesc(UUID variantId);

    Optional<ForecastRun> findFirstByVariantIdOrderByGeneratedAtDesc(UUID variantId);

    void deleteAllByVariantIdIn(List<UUID> variantIds);
}
