package com.dunghaiquyen.ecommerce.modules.returns.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReturnItemRequest(@NotNull UUID orderItemId, @Min(1) int quantity, String reason) {
}
