package com.dunghaiquyen.ecommerce.modules.replenishment.dto;

import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class GenerateForecastRequest {
    private List<UUID> variantIds;
}
