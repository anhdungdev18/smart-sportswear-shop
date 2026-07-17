package com.dunghaiquyen.ecommerce.modules.replenishment.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ForecastMetricResponse {
    private String algorithm;
    private BigDecimal mae;
    private BigDecimal wape;
    private String confidence;
}
