package com.dunghaiquyen.ecommerce.modules.promotion.repository;

import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionProduct;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromotionProductRepository extends JpaRepository<PromotionProduct, UUID> {

    @Query("select pp.product.id from PromotionProduct pp where pp.promotion.id = :promotionId")
    List<UUID> findProductIdsByPromotionId(@Param("promotionId") UUID promotionId);

    void deleteAllByPromotionId(UUID promotionId);
}
