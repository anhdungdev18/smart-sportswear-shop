package com.dunghaiquyen.ecommerce.modules.replenishment.repository;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.InventoryPolicy;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryPolicyRepository extends JpaRepository<InventoryPolicy, UUID> {

    Optional<InventoryPolicy> findByVariantId(UUID variantId);

    List<InventoryPolicy> findAllByVariantIdIn(List<UUID> variantIds);
}
