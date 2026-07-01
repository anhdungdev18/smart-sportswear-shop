package com.dunghaiquyen.ecommerce.modules.wishlist.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.wishlist.dto.AddWishlistItemRequest;
import com.dunghaiquyen.ecommerce.modules.wishlist.dto.WishlistResponse;
import com.dunghaiquyen.ecommerce.modules.wishlist.service.WishlistService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * No role restriction beyond SecurityConfig's default "anyRequest().authenticated()" -
 * any logged-in user (any role) may use their own wishlist.
 */
@RestController
@RequestMapping("/api/v1/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ApiResponse<WishlistResponse> getWishlist(@AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.ok(wishlistService.getWishlist(principal.getUserId()));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<WishlistResponse>> addItem(
            @AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody AddWishlistItemRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Item added", wishlistService.addItem(principal.getUserId(), body)));
    }

    @DeleteMapping("/items/{productId}")
    public ApiResponse<WishlistResponse> removeItem(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable UUID productId) {
        return ApiResponse.ok("Item removed", wishlistService.removeItem(principal.getUserId(), productId));
    }
}
