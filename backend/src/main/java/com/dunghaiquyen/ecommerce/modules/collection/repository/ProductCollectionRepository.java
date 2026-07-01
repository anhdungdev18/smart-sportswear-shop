package com.dunghaiquyen.ecommerce.modules.collection.repository;

import com.dunghaiquyen.ecommerce.modules.collection.entity.ProductCollection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCollectionRepository extends JpaRepository<ProductCollection, UUID> {

    List<ProductCollection> findAllByCollectionIdOrderBySortOrderAsc(UUID collectionId);

    Optional<ProductCollection> findByProductIdAndCollectionId(UUID productId, UUID collectionId);

    boolean existsByProductIdAndCollectionId(UUID productId, UUID collectionId);

    List<ProductCollection> findAllByProductIdOrderByCreatedAtAsc(UUID productId);
}
