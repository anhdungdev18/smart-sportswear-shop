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
 * Staff-facing shape: adds internalNote and a small customer reference
 * (needed for the admin list's "search by name/phone" requirement) on top of
 * the same fields as OrderResponse. Kept as its own record rather than
 * inheriting/wrapping OrderResponse - records cannot extend each other and
 * the duplication here is small and intentional.
 */
public record AdminOrderResponse(
        UUID id,
        String orderCode,
        UUID customerId,
        String customerName,
        String customerPhone,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        BigDecimal subtotalAmount,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String note,
        String internalNote,
        CancellationRequestedBy cancellationRequestedBy,
        String cancellationReason,
        Instant cancellationRequestedAt,
        List<OrderItemResponse> items,
        ShippingAddressResponse shippingAddress,
        Instant createdAt) {
}
