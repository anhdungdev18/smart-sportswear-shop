package com.dunghaiquyen.ecommerce.modules.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record PackingSlipBatchRequest(
        @NotEmpty
        @Size(max = 100)
        List<UUID> orderIds
) {
}
