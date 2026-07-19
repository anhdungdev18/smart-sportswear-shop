package com.dunghaiquyen.ecommerce.modules.replenishment.dto;

import lombok.Data;

@Data
public class ReplenishmentActionRequest {
    private Integer quantity; // Cho trường hợp adjust
    private String note;
}
