package com.dunghaiquyen.ecommerce.common.realtime;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class UiRealtimeService {
    private static final long TIMEOUT_MS = 30L * 60L * 1000L;
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public record DataChanged(String id, String scope, Instant occurredAt) {}

    public SseEmitter register() {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("ready").data(java.util.Map.of("connected", true)));
        } catch (IOException error) {
            emitters.remove(emitter);
            emitter.completeWithError(error);
        }
        return emitter;
    }

    public void publish(String path) {
        String[] parts = path.split("/");
        String scope = parts.length > 4 && "admin".equals(parts[3]) ? parts[4]
                : parts.length > 3 ? parts[3] : "application";
        DataChanged event = new DataChanged(UUID.randomUUID().toString(), scope, Instant.now());
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("data-changed").data(event));
            } catch (IOException | IllegalStateException error) {
                emitters.remove(emitter);
            }
        }
    }
}
