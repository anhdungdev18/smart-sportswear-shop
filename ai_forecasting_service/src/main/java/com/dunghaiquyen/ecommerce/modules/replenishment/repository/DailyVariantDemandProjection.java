package com.dunghaiquyen.ecommerce.modules.replenishment.repository;

import java.time.LocalDate;
import java.util.UUID;

public interface DailyVariantDemandProjection {

    UUID getVariantId();

    LocalDate getDemandDate();

    long getQuantity();
}
