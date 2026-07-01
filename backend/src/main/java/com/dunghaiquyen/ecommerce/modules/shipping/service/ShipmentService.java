package com.dunghaiquyen.ecommerce.modules.shipping.service;

import com.dunghaiquyen.ecommerce.common.exception.BusinessRuleException;
import com.dunghaiquyen.ecommerce.common.exception.ResourceNotFoundException;
import com.dunghaiquyen.ecommerce.modules.notification.service.NotificationService;
import com.dunghaiquyen.ecommerce.modules.order.entity.Order;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.order.repository.OrderRepository;
import com.dunghaiquyen.ecommerce.modules.shipping.dto.ShipmentResponse;
import com.dunghaiquyen.ecommerce.modules.shipping.dto.UpdateShipmentRequest;
import com.dunghaiquyen.ecommerce.modules.shipping.entity.Shipment;
import com.dunghaiquyen.ecommerce.modules.shipping.entity.ShipmentStatus;
import com.dunghaiquyen.ecommerce.modules.shipping.entity.ShippingMethod;
import com.dunghaiquyen.ecommerce.modules.shipping.repository.ShipmentRepository;
import com.dunghaiquyen.ecommerce.modules.shipping.repository.ShippingMethodRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logistics/tracking detail for an order, deliberately kept as its OWN,
 * independent state machine from Order.orderStatus:
 *
 * <p><b>Cardinality (chosen, simplest option):</b> exactly one Shipment per
 * Order. The V3 "shipments" table only indexes order_id (no unique
 * constraint - it was designed to allow split shipments later), but this
 * phase enforces "at most one" purely at this service layer rather than
 * adding a unique constraint via a new migration: a real split-shipment
 * feature is out of scope here, and a DB constraint would have to be undone
 * later anyway. Tradeoff accepted: the invariant only holds as long as every
 * write to this table goes through this service - documented, not enforced
 * by Postgres. The race that WOULD break it (two concurrent first-PATCH
 * calls for the same order both finding "no shipment yet") is closed by
 * locking the Order row first (orderRepository.findByIdForUpdate), the exact
 * same row-lock OrderService already uses to serialize concurrent status
 * updates - the second call blocks until the first commits its insert, then
 * re-reads and finds the shipment the first call just created.
 *
 * <p><b>Relationship to Order.orderStatus:</b> intentionally NOT synced
 * either direction. Order.orderStatus (PENDING_CONFIRMATION -> CONFIRMED ->
 * PACKING -> SHIPPING -> DELIVERED / CANCELLED) governs the customer-facing
 * lifecycle and inventory side effects (stock deduction/release) and is
 * still owned exclusively by OrderService.applyStatusTransition. Shipment's
 * own status (PENDING -> READY_TO_SHIP -> SHIPPING -> DELIVERED / FAILED /
 * RETURNED / CANCELLED) is operational/warehouse detail. Auto-syncing the two
 * was deliberately rejected: it would mean either this service reaching into
 * OrderService's transition rules (risking the two ALLOWED_TRANSITIONS maps
 * silently disagreeing) or OrderService reaching into this one (the same risk
 * the other direction) - exactly the "competing state machine" the task
 * warned against. The admin is expected to call both
 * PATCH /api/v1/admin/orders/{id}/status and PATCH .../{id}/shipping as two
 * separate, independent actions when both should move forward.
 *
 * <p><b>Tracking number:</b> may be set/changed at any point before the
 * shipment reaches a terminal status (DELIVERED/RETURNED/CANCELLED) - once
 * terminal, the tracking history is considered closed.
 *
 * <p><b>Delivered:</b> only reachable from SHIPPING, same as Order's own
 * SHIPPING -> DELIVERED step - a shipment cannot be marked delivered before
 * it was ever marked shipping.
 */
@Service
public class ShipmentService {

    /**
     * Linear-ish, mirrors OrderService.ALLOWED_TRANSITIONS' own style: every
     * step is a forward progression, plus FAILED/RETURNED/CANCELLED as the
     * exception exits a real carrier handoff can take.
     */
    private static final Map<ShipmentStatus, Set<ShipmentStatus>> ALLOWED_TRANSITIONS = Map.of(
            ShipmentStatus.PENDING, Set.of(ShipmentStatus.READY_TO_SHIP, ShipmentStatus.CANCELLED),
            ShipmentStatus.READY_TO_SHIP, Set.of(ShipmentStatus.SHIPPING, ShipmentStatus.CANCELLED),
            ShipmentStatus.SHIPPING, Set.of(ShipmentStatus.DELIVERED, ShipmentStatus.FAILED, ShipmentStatus.RETURNED),
            ShipmentStatus.DELIVERED, Set.of(),
            ShipmentStatus.FAILED, Set.of(ShipmentStatus.RETURNED, ShipmentStatus.CANCELLED),
            ShipmentStatus.RETURNED, Set.of(),
            ShipmentStatus.CANCELLED, Set.of());

    private static final Set<ShipmentStatus> TERMINAL_STATUSES =
            Set.of(ShipmentStatus.DELIVERED, ShipmentStatus.RETURNED, ShipmentStatus.CANCELLED);

    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final NotificationService notificationService;

    public ShipmentService(
            OrderRepository orderRepository,
            ShipmentRepository shipmentRepository,
            ShippingMethodRepository shippingMethodRepository,
            NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.shipmentRepository = shipmentRepository;
        this.shippingMethodRepository = shippingMethodRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getByOrderId(UUID orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Order not found");
        }
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No shipment exists yet for this order"));
        return toResponse(shipment);
    }

    /**
     * Creates the order's shipment on first call, updates it on every
     * subsequent call (lazy create-or-update, same pattern as Cart). See class
     * javadoc for why the order row is locked first.
     */
    @Transactional
    public ShipmentResponse upsertShipment(UUID orderId, UpdateShipmentRequest request, UUID actorId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        Shipment shipment = shipmentRepository.findByOrderId(orderId).orElseGet(() -> createShipmentFor(order));

        if (request.shippingMethodId() != null) {
            ShippingMethod method = shippingMethodRepository.findById(request.shippingMethodId())
                    .orElseThrow(() -> new ResourceNotFoundException("Shipping method not found"));
            if (method.getStatus() != com.dunghaiquyen.ecommerce.modules.shipping.entity.ShippingMethodStatus.ACTIVE) {
                throw new BusinessRuleException(HttpStatus.UNPROCESSABLE_ENTITY, "Shipping method is not available");
            }
            shipment.setShippingMethod(method);
        }

        if (request.trackingNumber() != null) {
            if (TERMINAL_STATUSES.contains(shipment.getStatus())) {
                throw new BusinessRuleException(
                        HttpStatus.CONFLICT,
                        "Cannot update tracking number for a " + shipment.getStatus() + " shipment");
            }
            UUID currentShipmentId = shipment.getId();
            shipmentRepository.findByTrackingNumber(request.trackingNumber())
                    .filter(existing -> !existing.getId().equals(currentShipmentId))
                    .ifPresent(existing -> {
                        throw new BusinessRuleException(
                                HttpStatus.CONFLICT, "Tracking number is already used by another shipment");
                    });
            shipment.setTrackingNumber(request.trackingNumber());
        }

        if (request.carrierName() != null) {
            shipment.setProvider(request.carrierName());
        }

        if (request.note() != null) {
            shipment.setNote(request.note());
        }

        if (request.status() != null) {
            applyStatusTransition(shipment, request.status(), order);
        }

        shipment = shipmentRepository.save(shipment);
        return toResponse(shipment);
    }

    private void applyStatusTransition(Shipment shipment, ShipmentStatus target, Order order) {
        ShipmentStatus current = shipment.getStatus();
        if (current == target) {
            return;
        }
        validateOrderAllowsShipmentTransition(order, target);
        if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            throw new BusinessRuleException(
                    HttpStatus.CONFLICT, "Cannot transition shipment from " + current + " to " + target);
        }
        shipment.setStatus(target);
        if (target == ShipmentStatus.SHIPPING) {
            if (shipment.getShippedAt() == null) {
                shipment.setShippedAt(Instant.now());
            }
            // See class javadoc: this is the precise "handed to a carrier" signal
            // the task asked for, independent of Order's own orderStatus.
            notificationService.notifyOrderShipping(order);
        } else if (target == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(Instant.now());
        }
    }

    private void validateOrderAllowsShipmentTransition(Order order, ShipmentStatus target) {
        OrderStatus orderStatus = order.getOrderStatus();
        if (orderStatus == OrderStatus.CANCELLED && target != ShipmentStatus.CANCELLED) {
            throw new BusinessRuleException(
                    HttpStatus.CONFLICT, "Cannot move shipment to " + target + " when order is CANCELLED");
        }
        if (target == ShipmentStatus.READY_TO_SHIP
                && orderStatus != OrderStatus.CONFIRMED
                && orderStatus != OrderStatus.PACKING
                && orderStatus != OrderStatus.SHIPPING
                && orderStatus != OrderStatus.DELIVERED) {
            throw new BusinessRuleException(
                    HttpStatus.CONFLICT, "Cannot mark shipment READY_TO_SHIP when order is " + orderStatus);
        }
        if (target == ShipmentStatus.SHIPPING && orderStatus != OrderStatus.SHIPPING && orderStatus != OrderStatus.DELIVERED) {
            throw new BusinessRuleException(
                    HttpStatus.CONFLICT, "Cannot mark shipment SHIPPING when order is " + orderStatus);
        }
        if (target == ShipmentStatus.DELIVERED && orderStatus != OrderStatus.DELIVERED) {
            throw new BusinessRuleException(
                    HttpStatus.CONFLICT, "Cannot mark shipment DELIVERED when order is " + orderStatus);
        }
    }

    private Shipment createShipmentFor(Order order) {
        Map<String, Object> snapshot = order.getAddressSnapshotJson();
        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setShipmentCode(generateShipmentCode());
        shipment.setStatus(ShipmentStatus.PENDING);
        shipment.setShippingFee(order.getShippingFee());
        shipment.setReceiverName(stringValue(snapshot, "receiverName"));
        shipment.setReceiverPhone(stringValue(snapshot, "phone"));
        shipment.setProvince(stringValue(snapshot, "province"));
        shipment.setDistrict(stringValue(snapshot, "district"));
        shipment.setWard(stringValue(snapshot, "ward"));
        shipment.setAddressLine(stringValue(snapshot, "addressLine"));
        try {
            return shipmentRepository.saveAndFlush(shipment);
        } catch (DataIntegrityViolationException ex) {
            // shipment_code collision (vanishingly unlikely with date+random
            // suffix) - same retry-once approach as OrderService.generateOrderCode.
            shipment.setShipmentCode(generateShipmentCode());
            return shipmentRepository.saveAndFlush(shipment);
        }
    }

    private String stringValue(Map<String, Object> snapshot, String key) {
        Object value = snapshot.get(key);
        return value != null ? value.toString() : null;
    }

    private String generateShipmentCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String randomPart = String.format("%05d", ThreadLocalRandom.current().nextInt(100000));
        return "SHP" + datePart + randomPart;
    }

    private ShipmentResponse toResponse(Shipment shipment) {
        ShippingMethod method = shipment.getShippingMethod();
        LocalDate from = null;
        LocalDate to = null;
        if (method != null) {
            LocalDate base = shipment.getShippedAt() != null
                    ? LocalDateTime.ofInstant(shipment.getShippedAt(), ZoneOffset.UTC).toLocalDate()
                    : LocalDate.now();
            from = method.getEstimatedDaysMin() != null ? base.plusDays(method.getEstimatedDaysMin()) : null;
            to = method.getEstimatedDaysMax() != null ? base.plusDays(method.getEstimatedDaysMax()) : null;
        }
        return new ShipmentResponse(
                shipment.getId(),
                shipment.getOrder().getId(),
                method != null ? method.getId() : null,
                method != null ? method.getName() : null,
                shipment.getShipmentCode(),
                shipment.getProvider(),
                shipment.getTrackingNumber(),
                shipment.getStatus(),
                shipment.getShippingFee(),
                shipment.getReceiverName(),
                shipment.getReceiverPhone(),
                shipment.getProvince(),
                shipment.getDistrict(),
                shipment.getWard(),
                shipment.getAddressLine(),
                shipment.getShippedAt(),
                shipment.getDeliveredAt(),
                from,
                to,
                shipment.getNote(),
                shipment.getCreatedAt(),
                shipment.getUpdatedAt());
    }
}
