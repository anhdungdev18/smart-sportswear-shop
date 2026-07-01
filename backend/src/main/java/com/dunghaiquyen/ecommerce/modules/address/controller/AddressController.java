package com.dunghaiquyen.ecommerce.modules.address.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.address.dto.AddressCreateRequest;
import com.dunghaiquyen.ecommerce.modules.address.dto.AddressResponse;
import com.dunghaiquyen.ecommerce.modules.address.dto.AddressUpdateRequest;
import com.dunghaiquyen.ecommerce.modules.address.service.AddressService;
import jakarta.validation.Valid;
import java.util.List;
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

/** Self-service only - any authenticated role may manage their own address book (same unrestricted-by-role pattern as MeController/WishlistController). */
@RestController
@RequestMapping("/api/v1/me/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ApiResponse<List<AddressResponse>> list(@AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.ok(addressService.listMine(principal.getUserId()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> create(
            @AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody AddressCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Address created", addressService.create(principal.getUserId(), request)));
    }

    @PatchMapping("/{id}")
    public ApiResponse<AddressResponse> update(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody AddressUpdateRequest request) {
        return ApiResponse.ok("Address updated", addressService.update(principal.getUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable UUID id) {
        addressService.delete(principal.getUserId(), id);
        return ApiResponse.ok("Address deleted", null);
    }

    @PatchMapping("/{id}/default")
    public ApiResponse<AddressResponse> setDefault(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable UUID id) {
        return ApiResponse.ok("Default address updated", addressService.setDefault(principal.getUserId(), id));
    }
}
