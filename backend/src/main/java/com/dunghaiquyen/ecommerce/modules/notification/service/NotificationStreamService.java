package com.dunghaiquyen.ecommerce.modules.notification.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * In-memory registry of live Server-Sent-Events connections, keyed by user.
 *
 * <p>Real-time delivery of in-app notifications: the storefront opens one
 * long-lived GET (EventSource) per browser tab; {@link NotificationService}
 * pushes a payload the instant a notification is persisted (after commit).
 *
 * <p>A user may hold several emitters at once (multiple tabs/devices) — each is
 * tracked and pushed to independently. State is per-instance and intentionally
 * ephemeral: on restart clients simply reconnect and re-fetch unread state via
 * the REST endpoints, so nothing here needs to survive a bounce. (Scaling past
 * one instance would need a shared pub/sub fan-out — out of scope this phase.)
 */
@Service
public class NotificationStreamService {

    private static final Logger log = LoggerFactory.getLogger(NotificationStreamService.class);

    /** Long idle timeout: notifications are sparse; the client reconnects if it lapses. */
    private static final long TIMEOUT_MS = 30 * 60 * 1000L;

    public static final String EVENT_NOTIFICATION = "notification";

    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /** Open a new stream for a user and register lifecycle cleanup. */
    public SseEmitter register(UUID userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onError(e -> remove(userId, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            remove(userId, emitter);
        });

        // Initial handshake so the client's onopen fires and proxies don't buffer.
        try {
            emitter.send(SseEmitter.event().name("ready").data("ok"));
        } catch (IOException ex) {
            remove(userId, emitter);
        }
        return emitter;
    }

    /** Push an event to every live stream a user currently holds; drop dead ones. */
    public void push(UUID userId, String event, Object data) {
        List<SseEmitter> list = emitters.get(userId);
        if (list == null || list.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event().name(event).data(data));
            } catch (IOException | IllegalStateException ex) {
                log.debug("SSE push failed for user={}, dropping emitter: {}", userId, ex.getMessage());
                remove(userId, emitter);
            }
        }
    }

    private void remove(UUID userId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }
}
