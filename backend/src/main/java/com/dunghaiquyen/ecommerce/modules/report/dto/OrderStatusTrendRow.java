package com.dunghaiquyen.ecommerce.modules.report.dto;

import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import java.time.LocalDate;

public interface OrderStatusTrendRow {
    LocalDate getBucket();

    OrderStatus getStatus();

    long getOrderCount();
}
