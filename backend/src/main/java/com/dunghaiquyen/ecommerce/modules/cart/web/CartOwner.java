package com.dunghaiquyen.ecommerce.modules.cart.web;

import java.util.UUID;

/** Exactly one of the two is set: a logged-in user's cart is keyed by userId, a guest's by sessionId. */
public record CartOwner(UUID userId, String sessionId) {

    public boolean isUser() {
        return userId != null;
    }
}
