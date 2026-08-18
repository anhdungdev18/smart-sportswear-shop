package com.dunghaiquyen.ecommerce.modules.notification.service;

import com.dunghaiquyen.ecommerce.common.mail.MailService;
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

/** Delivers committed notification rows outside the originating business transaction. */
@Service
public class NotificationDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryService.class);

    private final NotificationRepository notificationRepository;
    private final MailService mailService;

    public NotificationDeliveryService(
            NotificationRepository notificationRepository,
            MailService mailService) {
        this.notificationRepository = notificationRepository;
        this.mailService = mailService;
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

}
