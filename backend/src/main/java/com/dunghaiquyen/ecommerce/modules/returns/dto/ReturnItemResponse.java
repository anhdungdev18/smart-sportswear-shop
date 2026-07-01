package com.dunghaiquyen.ecommerce.modules.returns.dto;

import com.dunghaiquyen.ecommerce.modules.returns.entity.ReturnItemConditionStatus;
import com.dunghaiquyen.ecommerce.modules.returns.entity.ReturnItemResolution;
import java.math.BigDecimal;
import java.util.UUID;

public record ReturnItemResponse(
        UUID id,
        UUID orderItemId,
        String productName,
        String sku,
        int quantity,
        String reason,
        ReturnItemConditionStatus conditionStatus,
        ReturnItemResolution resolution,
        BigDecimal refundAmount) {
}
