package com.dunghaiquyen.ecommerce.modules.order.service;

import com.dunghaiquyen.ecommerce.config.AppOrderProperties;
import com.dunghaiquyen.ecommerce.modules.inventory.service.InventoryService;
import com.dunghaiquyen.ecommerce.modules.order.entity.Order;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.order.entity.PaymentMethod;
import com.dunghaiquyen.ecommerce.modules.order.repository.OrderItemRepository;
import com.dunghaiquyen.ecommerce.modules.order.repository.OrderRepository;
import com.dunghaiquyen.ecommerce.modules.payment.entity.PaymentStatus;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PendingPaymentExpiryService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryService inventoryService;
    private final AppOrderProperties properties;

    public PendingPaymentExpiryService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
            InventoryService inventoryService, AppOrderProperties properties) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.inventoryService = inventoryService;
        this.properties = properties;
    }

    @Scheduled(
            fixedDelayString = "${app.order.expiry-scan-delay-ms:60000}",
            initialDelayString = "${app.order.expiry-scan-initial-delay-ms:60000}")
    @Transactional
    public void expirePendingVnpayOrders() {
        Instant cutoff = Instant.now().minus(properties.effectivePendingPaymentExpiryMinutes(), ChronoUnit.MINUTES);
        List<Order> candidates = orderRepository
                .findTop100ByOrderStatusAndPaymentMethodAndPaymentStatusInAndCreatedAtBeforeOrderByCreatedAtAsc(
                        OrderStatus.PENDING_CONFIRMATION, PaymentMethod.VNPAY,
                        List.of(PaymentStatus.UNPAID, PaymentStatus.PENDING, PaymentStatus.FAILED, PaymentStatus.CANCELLED),
                        cutoff);
        for (Order candidate : candidates) {
            Order order = orderRepository.findByIdForUpdate(candidate.getId()).orElse(null);
            if (order == null || order.getOrderStatus() != OrderStatus.PENDING_CONFIRMATION
                    || order.getPaymentStatus() == PaymentStatus.PAID) continue;
            orderItemRepository.findAllByOrderIdOrderByIdAsc(order.getId()).stream()
                    .sorted(Comparator.comparing(item -> item.getVariant().getId()))
                    .forEach(item -> inventoryService.release(item.getVariant().getId(), item.getQuantity(), order, null));
            order.setOrderStatus(OrderStatus.CANCELLED);
            order.setPaymentStatus(PaymentStatus.CANCELLED);
            order.setInternalNote("Automatically cancelled because VNPay payment expired");
            orderRepository.save(order);
        }
    }
}
