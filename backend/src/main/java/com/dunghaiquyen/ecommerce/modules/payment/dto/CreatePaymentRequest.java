package com.dunghaiquyen.ecommerce.modules.payment.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreatePaymentRequest(@NotNull(message = "Order is required") UUID orderId) {
}
