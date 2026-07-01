package com.dunghaiquyen.ecommerce.modules.shipping.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * shippingFee is the number that would actually be charged at checkout
 * right now (computed by OrderService.calculateShippingFee - the same
 * flat-fee/free-threshold rule createOrderFromCart and the checkout preview
 * both use). shippingMethod.baseFee is informational only this phase: no
 * per-method fee differentiation is wired into the real charge yet (see
 * final report's tradeoffs) - exposed so the client can show "this method's
 * own listed fee" alongside the rule that currently actually applies,
 * without pretending they are already unified.
 */
public record ShippingFeePreviewResponse(
        UUID addressId,
        ShippingMethodResponse shippingMethod,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        boolean freeShippingApplied,
        LocalDate estimatedDeliveryDateFrom,
        LocalDate estimatedDeliveryDateTo) {
}
