package com.dunghaiquyen.ecommerce.modules.order.dto;

import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.order.entity.CancellationRequestedBy;
import com.dunghaiquyen.ecommerce.modules.order.entity.PaymentMethod;
import com.dunghaiquyen.ecommerce.modules.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Customer-facing shape, reused for create-response, "my orders" list, and
 * "my order" detail - deliberately has no internalNote (staff-only, see
 * AdminOrderResponse).
 */
public record OrderResponse(
        UUID id,
        String orderCode,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        BigDecimal subtotalAmount,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String note,
        CancellationRequestedBy cancellationRequestedBy,
        String cancellationReason,
        Instant cancellationRequestedAt,
        List<OrderItemResponse> items,
        ShippingAddressResponse shippingAddress,
        String invoiceNumber,
        boolean invoiceRequested,
        String invoiceCompanyName,
        String invoiceTaxCode,
        String invoiceCompanyAddress,
        Instant createdAt) {
}
