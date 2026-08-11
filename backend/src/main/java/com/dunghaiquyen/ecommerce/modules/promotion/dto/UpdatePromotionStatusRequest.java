package com.dunghaiquyen.ecommerce.modules.promotion.dto;

import com.dunghaiquyen.ecommerce.modules.promotion.entity.PromotionStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePromotionStatusRequest(@NotNull PromotionStatus status) {
}
