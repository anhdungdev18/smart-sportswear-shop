package com.dunghaiquyen.ecommerce.modules.shipping.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Shipping fee preview only depends on the selected address and shipping method. */
public record ShippingFeePreviewRequest(@NotNull UUID addressId, @NotNull UUID shippingMethodId) {
}
