package com.dunghaiquyen.ecommerce.modules.order.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.modules.order.entity.Order;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.order.entity.PaymentMethod;
import com.dunghaiquyen.ecommerce.modules.payment.entity.PaymentStatus;
import org.springframework.http.HttpStatus;

/**
 * Cross-state rules between fulfillment and payment. Keeping these checks in
 * one place prevents admin and customer transition paths from drifting apart.
 */
final class OrderFinancialPolicy {

    private OrderFinancialPolicy() {
    }

    static void validateTransition(Order order, OrderStatus target) {
        if (target == OrderStatus.CANCELLED && order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BusinessRuleException(
                    HttpStatus.CONFLICT,
                    "Paid order must be refunded before cancellation");
        }
        if ((target == OrderStatus.CONFIRMED
                || target == OrderStatus.PACKING
                || target == OrderStatus.SHIPPING
                || target == OrderStatus.DELIVERED)
                && order.getPaymentMethod() == PaymentMethod.VNPAY
                && order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new BusinessRuleException(
                    HttpStatus.CONFLICT,
                    "VNPAY order must be paid before confirmation");
        }
    }
}
