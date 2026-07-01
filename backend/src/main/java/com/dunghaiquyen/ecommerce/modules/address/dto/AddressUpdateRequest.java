package com.dunghaiquyen.ecommerce.modules.address.dto;

import com.dunghaiquyen.ecommerce.common.validation.NullOrNotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** PATCH semantics: every field optional, null means "leave unchanged". isDefault is deliberately not here - use PATCH .../default instead, so "set default" stays one explicit action with its own unset-others side effect. */
public record AddressUpdateRequest(

        @NullOrNotBlank(message = "Receiver name must not be blank")
        @Size(max = 150, message = "Receiver name must be at most 150 characters")
        String receiverName,

        @NullOrNotBlank(message = "Phone must not be blank")
        @Pattern(regexp = "^[0-9+\\-() ]{8,20}$", message = "Phone is invalid")
        String phone,

        @NullOrNotBlank(message = "Province must not be blank")
        @Size(max = 100, message = "Province must be at most 100 characters")
        String province,

        @NullOrNotBlank(message = "District must not be blank")
        @Size(max = 100, message = "District must be at most 100 characters")
        String district,

        @NullOrNotBlank(message = "Ward must not be blank")
        @Size(max = 100, message = "Ward must be at most 100 characters")
        String ward,

        @NullOrNotBlank(message = "Address line must not be blank")
        @Size(max = 255, message = "Address line must be at most 255 characters")
        String addressLine) {
}
