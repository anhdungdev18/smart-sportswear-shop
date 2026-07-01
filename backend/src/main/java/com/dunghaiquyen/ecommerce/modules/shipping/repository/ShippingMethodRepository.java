package com.dunghaiquyen.ecommerce.modules.shipping.repository;

import com.dunghaiquyen.ecommerce.modules.shipping.entity.ShippingMethod;
import com.dunghaiquyen.ecommerce.modules.shipping.entity.ShippingMethodStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, UUID> {

    List<ShippingMethod> findAllByStatusOrderByBaseFeeAsc(ShippingMethodStatus status);
}
