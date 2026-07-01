package com.dunghaiquyen.ecommerce.modules.shipping.repository;

import com.dunghaiquyen.ecommerce.modules.shipping.entity.Shipment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    /**
     * This phase's chosen cardinality is exactly one shipment per order, even
     * though the V3 schema only indexes order_id (no unique constraint) -
     * enforced at the service layer (see ShipmentService) rather than via a
     * new migration. findBy, not findAllBy: a second row appearing here would
     * be a bug in that enforcement, not an expected multi-shipment case.
     */
    Optional<Shipment> findByOrderId(UUID orderId);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}
