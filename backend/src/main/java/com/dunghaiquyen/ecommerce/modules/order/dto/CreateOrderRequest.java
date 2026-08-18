package com.dunghaiquyen.ecommerce.modules.order.dto;

import com.dunghaiquyen.ecommerce.modules.order.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "Address is required") UUID addressId,
        @NotNull(message = "Payment method is required") PaymentMethod paymentMethod,
        String note,
        List<UUID> cartItemIds,
        UUID buyNowVariantId,
        Integer buyNowQuantity,
        // Company (VAT-style) invoice request - all three required together
        // when invoiceRequested is true, validated in OrderService since it is
        // a cross-field rule.
        Boolean invoiceRequested,
        String invoiceCompanyName,
        String invoiceTaxCode,
        String invoiceCompanyAddress) {
}
