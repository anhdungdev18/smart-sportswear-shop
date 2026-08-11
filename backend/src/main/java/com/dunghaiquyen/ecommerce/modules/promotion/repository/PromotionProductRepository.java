package com.dunghaiquyen.ecommerce.modules.promotion.repository;

import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionProduct;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PromotionProductRepository extends JpaRepository<PromotionProduct, UUID> {

    List<PromotionProduct> findByPromotionId(UUID promotionId);

    @Transactional
    void deleteByPromotionId(UUID promotionId);
}
