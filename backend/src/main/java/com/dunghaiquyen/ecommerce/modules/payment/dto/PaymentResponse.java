package com.dunghaiquyen.ecommerce.modules.payment.dto;

import com.dunghaiquyen.ecommerce.modules.payment.entity.PaymentProvider;
import com.dunghaiquyen.ecommerce.modules.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** API_SPEC_PHASE1.md 8.3 - full attempt history for an order (one row per create-session call). */
public record PaymentResponse(
        UUID id,
        PaymentProvider provider,
        String transactionRef,
        BigDecimal amount,
        PaymentStatus status,
        String gatewayTransactionNo,
        String bankCode,
        Instant paidAt,
        Instant createdAt) {
}
