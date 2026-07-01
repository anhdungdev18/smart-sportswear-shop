package com.dunghaiquyen.ecommerce.modules.returns.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractAuditEntity;
import com.dunghaiquyen.ecommerce.modules.order.entity.Order;
import com.dunghaiquyen.ecommerce.modules.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * A customer's return request for one order (this phase's chosen
 * cardinality: an order may have multiple Return rows over time, but at
 * most one in a non-terminal status at once - enforced in ReturnService,
 * not by a DB constraint, same tradeoff Shipment's "one per order" rule
 * already accepts). REFUNDED is only ever reached as a side effect of a
 * linked Refund completing (see ReturnService.updateRefundStatus) - never
 * set directly by the status-update endpoint.
 */
@Getter
@Setter
@Entity
@Table(name = "returns")
public class Return extends AbstractAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "return_code", nullable = false, unique = true, length = 50)
    private String returnCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReturnStatus status = ReturnStatus.REQUESTED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private ReturnReason reason;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "received_at")
    private Instant receivedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by")
    private User handledBy;
}
