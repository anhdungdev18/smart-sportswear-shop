package com.dunghaiquyen.ecommerce.modules.checkout.dto;

import java.util.UUID;

/**
 * addressId is optional and only checked for ownership when provided (there is
 * no address-dependent shipping rule yet, so a preview without one still yields
 * a usable subtotal/shipping/total). The combo discount is derived from the cart
 * itself, so no discount input is needed here.
 */
public record CheckoutPreviewRequest(UUID addressId) {
}
