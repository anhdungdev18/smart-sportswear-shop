package com.dunghaiquyen.ecommerce.modules.notification.entity;

import com.dunghaiquyen.ecommerce.common.entity.AbstractCreatedAtEntity;
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
 * One row per attempted notification (Phase O). user/order are both nullable
 * - PASSWORD_RESET has no order; a notification tied to a since-deleted
 * user/order still keeps its row (on delete set null) since this is a send
 * history, not a live reference. Written PENDING first, then updated to
 * SENT/FAILED in the same transaction by NotificationService - see its class
 * javadoc for why a failed send never rolls back the business action that
 * triggered it.
 */
@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification extends AbstractCreatedAtEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel = NotificationChannel.EMAIL;

    @Column(nullable = false, length = 255)
    private String recipient;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(columnDefinition = "text")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "sent_at")
    private Instant sentAt;

    /**
     * In-app read state (Notification Operations): null = unread, a timestamp =
     * when the recipient viewed it in the storefront inbox. Orthogonal to
     * {@link #status}, which records the EMAIL send outcome, not whether the
     * user has seen it in-app.
     */
    @Column(name = "read_at")
    private Instant readAt;

    /**
     * Set only on a row CREATED by a resend (points back to the original it
     * resends); null on every organic/original row. Resend always creates a
     * new row rather than mutating the original - see NotificationService.
     * resend's javadoc for the audit-trail tradeoff this chooses.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resend_of_id")
    private Notification resendOf;

    /** Bookkeeping kept on the ORIGINAL row only: how many resend attempts have been spawned from it, for the max-attempts rule. */
    @Column(name = "resend_count", nullable = false)
    private int resendCount = 0;

    /** Bookkeeping kept on the ORIGINAL row only: when the last resend attempt was made, for the cooldown rule. */
    @Column(name = "last_resend_at")
    private Instant lastResendAt;
}
