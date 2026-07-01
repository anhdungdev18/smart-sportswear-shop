package com.dunghaiquyen.ecommerce.modules.product.repository;

import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariantRepository
        extends JpaRepository<ProductVariant, UUID>, JpaSpecificationExecutor<ProductVariant> {

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, UUID id);

    List<ProductVariant> findAllByProductIdOrderByCreatedAtAsc(UUID productId);

    List<ProductVariant> findAllByProductIdIn(List<UUID> productIds);

    /**
     * Row-locked read used by InventoryService before any stock_quantity /
     * reserved_quantity mutation - the lock is held for the rest of the
     * caller's transaction, not just this one statement.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from ProductVariant v where v.id = :id")
    Optional<ProductVariant> findByIdForUpdate(@Param("id") UUID id);
}
