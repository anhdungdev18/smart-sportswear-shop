package com.dunghaiquyen.ecommerce.modules.checkout.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateCouponRequest(@NotBlank(message = "couponCode is required") String couponCode) {
}
