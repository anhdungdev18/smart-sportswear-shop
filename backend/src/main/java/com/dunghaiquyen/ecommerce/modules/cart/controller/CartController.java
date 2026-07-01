package com.dunghaiquyen.ecommerce.modules.cart.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.cart.dto.AddCartItemRequest;
import com.dunghaiquyen.ecommerce.modules.cart.dto.CartResponse;
import com.dunghaiquyen.ecommerce.modules.cart.dto.UpdateCartItemRequest;
import com.dunghaiquyen.ecommerce.modules.cart.service.CartService;
import com.dunghaiquyen.ecommerce.modules.cart.web.CartIdentityResolver;
import com.dunghaiquyen.ecommerce.modules.cart.web.CartOwner;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public per API_SPEC_PHASE1.md 6.1-6.4 - no role/JWT requirement. Identity
 * (logged-in user vs guest session) is resolved per request by
 * CartIdentityResolver, not by this controller.
 */
@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;
    private final CartIdentityResolver identityResolver;

    public CartController(CartService cartService, CartIdentityResolver identityResolver) {
        this.cartService = cartService;
        this.identityResolver = identityResolver;
    }

    @GetMapping
    public ApiResponse<CartResponse> getCart(
            HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal CustomUserDetails principal) {
        CartOwner owner = identityResolver.resolve(request, response, principal);
        return ApiResponse.ok(cartService.getCart(owner));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            HttpServletRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody AddCartItemRequest body) {
        CartOwner owner = identityResolver.resolve(request, response, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Item added", cartService.addItem(owner, body)));
    }

    @PatchMapping("/items/{id}")
    public ApiResponse<CartResponse> updateItem(
            HttpServletRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCartItemRequest body) {
        CartOwner owner = identityResolver.resolve(request, response, principal);
        return ApiResponse.ok("Item updated", cartService.updateItemQuantity(owner, id, body));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<CartResponse> removeItem(
            HttpServletRequest request,
            HttpServletResponse response,
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id) {
        CartOwner owner = identityResolver.resolve(request, response, principal);
        return ApiResponse.ok("Item removed", cartService.removeItem(owner, id));
    }
}
