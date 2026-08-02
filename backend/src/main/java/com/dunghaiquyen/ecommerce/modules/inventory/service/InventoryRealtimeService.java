package com.dunghaiquyen.ecommerce.modules.inventory.service;

import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class InventoryRealtimeService {
    private static final long TIMEOUT_MS = 30L * 60L * 1000L;
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public record StockChanged(UUID variantId, UUID productId, int stockQuantity,
            int reservedQuantity, int availableQuantity) {}

    public SseEmitter register() {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("ready").data(Map.of("connected", true)));
        } catch (IOException error) {
            emitters.remove(emitter);
            emitter.completeWithError(error);
        }
        return emitter;
    }

    public void publishAfterCommit(ProductVariant variant) {
        StockChanged event = new StockChanged(
                variant.getId(), variant.getProduct().getId(), variant.getStockQuantity(),
                variant.getReservedQuantity(), Math.max(0, variant.getStockQuantity() - variant.getReservedQuantity()));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { broadcast(event); }
            });
        } else {
            broadcast(event);
        }
    }

    private void broadcast(StockChanged event) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("stock-changed").data(event));
            } catch (IOException | IllegalStateException error) {
                emitters.remove(emitter);
            }
        }
    }
}
