package com.dunghaiquyen.ecommerce.modules.inventory.controller;

import com.dunghaiquyen.ecommerce.modules.inventory.service.InventoryRealtimeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryRealtimeController {
    private final InventoryRealtimeService realtimeService;

    public InventoryRealtimeController(InventoryRealtimeService realtimeService) {
        this.realtimeService = realtimeService;
    }

    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter stream() {
        return realtimeService.register();
    }
}
