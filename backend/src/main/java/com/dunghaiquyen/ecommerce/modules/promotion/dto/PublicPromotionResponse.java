package com.dunghaiquyen.ecommerce.modules.promotion.dto;

import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionScope;
import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Deliberately narrower than the admin PromotionResponse: no status (the
 * /active listing already filters to it), no usageLimit/usageCount (internal
 * redemption metrics, not something a shopper needs to make a decision), no
 * createdAt/updatedAt/createdBy (operational metadata). productIds is kept -
 * it is exactly what lets the FE highlight which products a PRODUCT-scoped
 * campaign applies to, which is the whole point of a public listing.
 */
public record PublicPromotionResponse(
        UUID id,
        String name,
        String slug,
        String description,
        PromotionType type,
        PromotionScope scope,
        BigDecimal discountPercent,
        BigDecimal discountAmount,
        BigDecimal minOrderAmount,
        BigDecimal maxDiscountAmount,
        Instant startsAt,
        Instant endsAt,
        List<UUID> productIds) {
}
