package com.dunghaiquyen.ecommerce.modules.returns.dto;

import com.dunghaiquyen.ecommerce.modules.returns.entity.ReturnItemConditionStatus;
import com.dunghaiquyen.ecommerce.modules.returns.entity.ReturnItemResolution;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/** Submitted only when transitioning a Return to RECEIVED - see ReturnService.updateStatus. */
public record ReturnItemResolutionRequest(
        @NotNull UUID returnItemId,
        @NotNull ReturnItemConditionStatus conditionStatus,
        @NotNull ReturnItemResolution resolution,
        BigDecimal refundAmount) {
}
