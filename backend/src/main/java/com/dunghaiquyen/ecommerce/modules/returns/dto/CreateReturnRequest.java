package com.dunghaiquyen.ecommerce.modules.returns.dto;

import com.dunghaiquyen.ecommerce.modules.returns.entity.ReturnReason;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateReturnRequest(
        @NotNull UUID orderId,
        @NotNull ReturnReason reason,
        String description,
        @NotEmpty @Valid List<ReturnItemRequest> items) {
}
