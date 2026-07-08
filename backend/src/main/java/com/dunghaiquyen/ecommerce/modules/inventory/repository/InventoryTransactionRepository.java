package com.dunghaiquyen.ecommerce.modules.inventory.repository;

import com.dunghaiquyen.ecommerce.modules.inventory.entity.InventoryTransaction;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** InventoryService appends a row here for every stock_quantity/reserved_quantity mutation. */
public interface InventoryTransactionRepository
        extends JpaRepository<InventoryTransaction, UUID>, JpaSpecificationExecutor<InventoryTransaction> {

    void deleteAllByVariantIdIn(List<UUID> variantIds);
}
