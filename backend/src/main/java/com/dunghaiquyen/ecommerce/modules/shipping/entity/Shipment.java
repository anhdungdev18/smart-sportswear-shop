package com.dunghaiquyen.ecommerce.modules.shipping.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractAuditEntity;
import com.dunghaiquyen.ecommerce.modules.order.entity.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * One row per order's logistics/tracking detail (this phase's chosen
 * cardinality: exactly one shipment per order, see ShipmentService javadoc).
 * Maps onto the existing V3 "shipments" table - no new migration. Order does
 * NOT get a back-reference field; callers look this up via
 * ShipmentRepository.findByOrderId, so the Order entity/module is untouched.
 */
@Getter
@Setter
@Entity
@Table(name = "shipments")
public class Shipment extends AbstractAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_method_id")
    private ShippingMethod shippingMethod;

    @Column(name = "shipment_code", nullable = false, unique = true, length = 50)
    private String shipmentCode;

    /** Carrier display name - V3's existing "provider" column, reused as the carrierName the admin sets. */
    @Column(length = 50)
    private String provider;

    @Column(name = "tracking_number", unique = true, length = 120)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status = ShipmentStatus.PENDING;

    @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "receiver_name", nullable = false, length = 150)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    @Column(nullable = false, length = 100)
    private String province;

    /** Vietnam dropped this administrative tier in the 2025 reform - kept nullable for legacy rows. */
    @Column(length = 100)
    private String district;

    @Column(nullable = false, length = 100)
    private String ward;

    @Column(name = "address_line", nullable = false, length = 255)
    private String addressLine;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(columnDefinition = "text")
    private String note;
}
