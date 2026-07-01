package com.dunghaiquyen.ecommerce.modules.returns.dto;

import com.dunghaiquyen.ecommerce.modules.returns.entity.RefundStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateRefundStatusRequest(@NotNull RefundStatus status) {
}
