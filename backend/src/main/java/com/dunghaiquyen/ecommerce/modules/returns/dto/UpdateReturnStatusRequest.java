package com.dunghaiquyen.ecommerce.modules.returns.dto;

import com.dunghaiquyen.ecommerce.modules.returns.entity.ReturnStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** items is required (and must cover every item on this return) only when status == RECEIVED - see ReturnService.updateStatus. */
public record UpdateReturnStatusRequest(@NotNull ReturnStatus status, @Valid List<ReturnItemResolutionRequest> items) {
}
