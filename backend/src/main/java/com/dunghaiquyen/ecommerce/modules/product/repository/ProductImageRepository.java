package com.dunghaiquyen.ecommerce.modules.product.repository;

import com.dunghaiquyen.ecommerce.modules.product.entity.ProductImage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    Optional<ProductImage> findByPublicId(String publicId);

    List<ProductImage> findAllByProductIdOrderBySortOrderAsc(UUID productId);

    List<ProductImage> findAllByProductIdIn(List<UUID> productIds);

    List<ProductImage> findAllByProductIdAndPrimaryTrue(UUID productId);
}
