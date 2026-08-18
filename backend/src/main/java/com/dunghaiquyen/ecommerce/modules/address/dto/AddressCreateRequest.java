package com.dunghaiquyen.ecommerce.modules.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressCreateRequest(

        @NotBlank(message = "Receiver name is required")
        @Size(max = 150, message = "Receiver name must be at most 150 characters")
        String receiverName,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[0-9+\\-() ]{8,20}$", message = "Phone is invalid")
        String phone,

        @NotBlank(message = "Province is required")
        @Size(max = 100, message = "Province must be at most 100 characters")
        String province,

        // Optional: Vietnam's 2025 administrative reform dropped this tier
        // (addresses are now Tinh/Thanh pho -> Phuong/Xa directly).
        @Size(max = 100, message = "District must be at most 100 characters")
        String district,

        @NotBlank(message = "Ward is required")
        @Size(max = 100, message = "Ward must be at most 100 characters")
        String ward,

        @NotBlank(message = "Address line is required")
        @Size(max = 255, message = "Address line must be at most 255 characters")
        String addressLine,

        Boolean isDefault) {
}
