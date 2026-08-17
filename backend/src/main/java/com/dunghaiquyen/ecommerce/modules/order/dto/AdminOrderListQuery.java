package com.dunghaiquyen.ecommerce.modules.order.dto;

import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.order.entity.PaymentMethod;
import com.dunghaiquyen.ecommerce.modules.payment.entity.PaymentStatus;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Admin order list query (API_SPEC_PHASE1.md 10.1). "keyword" covers spec's
 * "tim theo ma don, ten, so dien thoai" via a single param matched against
 * order_code OR the ordering user's fullName/phone.
 */
public record AdminOrderListQuery(
        Integer page,
        Integer limit,
        String keyword,
        UUID customerId,
        OrderStatus status,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        LocalDate dateFrom,
        LocalDate dateTo) {
}
