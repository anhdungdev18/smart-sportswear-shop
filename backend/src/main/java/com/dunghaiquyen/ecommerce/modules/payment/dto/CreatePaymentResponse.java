package com.dunghaiquyen.ecommerce.modules.payment.dto;

/** API_SPEC_PHASE1.md 8.1 response shape. */
public record CreatePaymentResponse(String paymentUrl, String transactionRef) {
}
