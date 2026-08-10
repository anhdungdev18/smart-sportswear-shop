package com.dunghaiquyen.ecommerce.modules.returns.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmManualRefundRequest(
        @NotBlank @Size(max = 100) String reference,
        @Size(max = 1000) String note) {
}
