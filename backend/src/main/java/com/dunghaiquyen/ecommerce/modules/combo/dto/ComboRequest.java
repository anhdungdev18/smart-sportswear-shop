package com.dunghaiquyen.ecommerce.modules.combo.dto;

import com.dunghaiquyen.ecommerce.modules.combo.entity.ComboStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Create/update payload for a combo. status defaults to ACTIVE when omitted. */
public record ComboRequest(
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal discountAmount,
        ComboStatus status,
        @NotEmpty List<UUID> productIds) {
}
