package com.dunghaiquyen.ecommerce.modules.returns.entity;

/** Exact set V3's chk_refunds_provider check constraint allows. MANUAL covers any refund with no matching Payment row (e.g. COD orders that never created a payment session). */
public enum RefundProvider {
    COD,
    VNPAY,
    MANUAL
}
