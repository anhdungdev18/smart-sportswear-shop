package com.dunghaiquyen.ecommerce.modules.order.dto;

import com.dunghaiquyen.ecommerce.modules.order.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull(message = "Address is required") UUID addressId,
        @NotNull(message = "Payment method is required") PaymentMethod paymentMethod,
        String note,
        // Optional (Phase N2). At most one coupon per order - see CouponService's class javadoc.
        String couponCode) {
}
