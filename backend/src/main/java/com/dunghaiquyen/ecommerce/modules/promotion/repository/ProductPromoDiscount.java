package com.dunghaiquyen.ecommerce.modules.promotion.repository;

import java.math.BigDecimal;
import java.util.UUID;

/** Projection: the best active percentage discount for a product. */
public interface ProductPromoDiscount {
    UUID getProductId();

    BigDecimal getPercent();
}
