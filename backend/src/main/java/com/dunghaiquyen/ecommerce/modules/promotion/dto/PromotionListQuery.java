package com.dunghaiquyen.ecommerce.modules.promotion.dto;

import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionStatus;

public record PromotionListQuery(Integer page, Integer limit, PromotionStatus status) {
}
