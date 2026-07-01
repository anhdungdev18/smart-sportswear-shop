package com.dunghaiquyen.ecommerce.modules.shipping.service;

import com.dunghaiquyen.ecommerce.modules.shipping.dto.ShippingMethodResponse;
import com.dunghaiquyen.ecommerce.modules.shipping.entity.ShippingMethod;
import com.dunghaiquyen.ecommerce.modules.shipping.entity.ShippingMethodStatus;
import com.dunghaiquyen.ecommerce.modules.shipping.repository.ShippingMethodRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Public catalog of shipping methods (GET /api/v1/shipping/methods). Only
 * ACTIVE methods are exposed to guests/customers - an INACTIVE method still
 * exists for admin/historical reference (e.g. an old shipment that used it)
 * but must not be offered for new selection.
 */
@Service
public class ShippingMethodService {

    private final ShippingMethodRepository shippingMethodRepository;

    public ShippingMethodService(ShippingMethodRepository shippingMethodRepository) {
        this.shippingMethodRepository = shippingMethodRepository;
    }

    @Transactional(readOnly = true)
    public List<ShippingMethodResponse> listAvailable() {
        return shippingMethodRepository.findAllByStatusOrderByBaseFeeAsc(ShippingMethodStatus.ACTIVE).stream()
                .map(ShippingMethodService::toResponse)
                .toList();
    }

    static ShippingMethodResponse toResponse(ShippingMethod method) {
        return new ShippingMethodResponse(
                method.getId(),
                method.getName(),
                method.getCode(),
                method.getDescription(),
                method.getProvider(),
                method.getBaseFee(),
                method.getEstimatedDaysMin(),
                method.getEstimatedDaysMax());
    }
}
