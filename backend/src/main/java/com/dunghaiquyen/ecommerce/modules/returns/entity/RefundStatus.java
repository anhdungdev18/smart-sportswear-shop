package com.dunghaiquyen.ecommerce.modules.returns.entity;

/**
 * Exact set V3's chk_refunds_status check constraint allows. This phase only
 * ever produces PENDING (on creation) then COMPLETED/FAILED/CANCELLED (admin
 * PATCH) - PROCESSING exists in the schema for a future real payment-gateway
 * refund-API integration (there is none this phase, same as PaymentService's
 * VNPay flow being sandbox-only) and is unused for now.
 */
public enum RefundStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED
}
