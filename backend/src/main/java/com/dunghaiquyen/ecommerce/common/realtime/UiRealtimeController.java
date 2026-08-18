package com.dunghaiquyen.ecommerce.common.realtime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/realtime")
public class UiRealtimeController {
    private final UiRealtimeService realtimeService;

    public UiRealtimeController(UiRealtimeService realtimeService) {
        this.realtimeService = realtimeService;
    }

    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter stream() {
        return realtimeService.register();
    }
}
