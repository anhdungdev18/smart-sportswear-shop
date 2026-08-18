package com.dunghaiquyen.ecommerce.modules.order.repository;

import com.dunghaiquyen.ecommerce.modules.order.entity.Order;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;
import java.util.List;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.order.entity.PaymentMethod;
import com.dunghaiquyen.ecommerce.modules.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    boolean existsByNote(String note);

    /**
     * Row-locked read used by PaymentService.createPaymentSession to serialize
     * concurrent "create a session for this order" calls - the second caller
     * blocks here until the first commits (creating its PENDING payment row),
     * then re-reads the order's latest payment fresh and reuses that row
     * instead of racing to insert a second PENDING attempt for the same order.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") UUID id);

    List<Order> findTop100ByOrderStatusAndPaymentMethodAndPaymentStatusInAndCreatedAtBeforeOrderByCreatedAtAsc(
            OrderStatus orderStatus, PaymentMethod paymentMethod, List<PaymentStatus> paymentStatuses, Instant cutoff);

    /** Backs OrderService.generateInvoiceNumber - a real DB sequence so concurrent invoice issuance never collides. */
    @Query(value = "select nextval('orders_invoice_number_seq')", nativeQuery = true)
    long nextInvoiceSequence();
}
