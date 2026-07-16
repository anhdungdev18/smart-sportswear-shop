package com.dunghaiquyen.ecommerce.modules.notification.repository;

import com.dunghaiquyen.ecommerce.modules.notification.entity.Notification;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {

    /** In-app unread badge count for a user (read_at is null). */
    long countByUserIdAndReadAtIsNull(UUID userId);

    /** Mark every unread notification of a user as read in one statement; returns rows updated. */
    @Modifying
    @Query("update Notification n set n.readAt = :now where n.user.id = :userId and n.readAt is null")
    int markAllReadForUser(@Param("userId") UUID userId, @Param("now") Instant now);

    /**
     * Row-locked read used by NotificationService.resend to serialize two
     * concurrent resend calls against the same original notification - the
     * second blocks here until the first commits its resendCount/
     * lastResendAt update, then re-reads the now-advanced counters before
     * its own cap/cooldown check, the same pattern OrderRepository.
     * findByIdForUpdate already establishes for order status transitions.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from Notification n where n.id = :id")
    Optional<Notification> findByIdForUpdate(@Param("id") UUID id);
}
