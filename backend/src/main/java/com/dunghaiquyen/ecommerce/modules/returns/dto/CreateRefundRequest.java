package com.dunghaiquyen.ecommerce.modules.returns.dto;

/** No amount field - the amount is always computed server-side from the return's own items' refundAmount (resolution == REFUND), never admin-entered, so it can never drift from what was actually resolved at RECEIVED time. */
public record CreateRefundRequest(String reason) {
}
