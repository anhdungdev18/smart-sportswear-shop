package com.dunghaiquyen.ecommerce.modules.returns.dto;

import com.dunghaiquyen.ecommerce.modules.returns.entity.ReturnStatus;
import java.util.UUID;

public record AdminReturnListQuery(Integer page, Integer limit, ReturnStatus status, UUID userId, UUID orderId) {
}
