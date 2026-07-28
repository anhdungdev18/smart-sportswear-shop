package com.dunghaiquyen.ecommerce.modules.notification.service;

import com.dunghaiquyen.ecommerce.common.mail.MailService;
import com.dunghaiquyen.ecommerce.modules.notification.dto.NotificationResponse;
import com.dunghaiquyen.ecommerce.modules.notification.entity.Notification;
import com.dunghaiquyen.ecommerce.modules.notification.entity.NotificationStatus;
import com.dunghaiquyen.ecommerce.modules.notification.repository.NotificationRepository;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Delivers committed notification rows outside the originating business transaction. */
@Service
public class NotificationDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryService.class);

    private final NotificationRepository notificationRepository;
    private final MailService mailService;
    private final NotificationBroadcaster broadcaster;

    public NotificationDeliveryService(
            NotificationRepository notificationRepository,
            MailService mailService,
            NotificationBroadcaster broadcaster) {
        this.notificationRepository = notificationRepository;
        this.mailService = mailService;
        this.broadcaster = broadcaster;
    }

    @Async("notificationTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deliverAsync(UUID notificationId) {
        Notification notification = notificationRepository.findByIdForUpdate(notificationId).orElse(null);
        if (notification == null || notification.getStatus() != NotificationStatus.PENDING) {
            return;
        }
        try {
            mailService.send(notification.getRecipient(), notification.getSubject(), notification.getBody());
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            notification.setErrorMessage(null);
        } catch (RuntimeException ex) {
            log.warn(
                    "Notification send failed type={} recipient={}: {}",
                    notification.getType(), notification.getRecipient(), ex.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(ex.getMessage());
        }
        notificationRepository.save(notification);
        broadcastAfterCommit(notification);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSchedulingFailed(UUID notificationId, String errorMessage) {
        notificationRepository.findByIdForUpdate(notificationId).ifPresent(notification -> {
            if (notification.getStatus() == NotificationStatus.PENDING) {
                notification.setStatus(NotificationStatus.FAILED);
                notification.setErrorMessage(errorMessage);
                notificationRepository.save(notification);
            }
        });
    }

    private void broadcastAfterCommit(Notification notification) {
        if (notification.getUser() == null) {
            return;
        }
        UUID userId = notification.getUser().getId();
        NotificationResponse payload = toResponse(notification);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                broadcaster.broadcast(userId, payload);
            }
        });
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUser() != null ? notification.getUser().getId() : null,
                notification.getOrder() != null ? notification.getOrder().getId() : null,
                notification.getType(),
                notification.getChannel(),
                notification.getRecipient(),
                notification.getSubject(),
                notification.getBody(),
                notification.getStatus(),
                notification.getErrorMessage(),
                notification.getCreatedAt(),
                notification.getSentAt(),
                notification.getReadAt(),
                notification.getResendOf() != null ? notification.getResendOf().getId() : null,
                notification.getResendCount(),
                notification.getLastResendAt());
    }
}
