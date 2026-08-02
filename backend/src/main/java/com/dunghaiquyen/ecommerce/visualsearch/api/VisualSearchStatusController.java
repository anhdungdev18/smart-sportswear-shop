package com.dunghaiquyen.ecommerce.visualsearch.api;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/visual-search")
public class VisualSearchStatusController {
    private final VisualSearchProperties properties;
    private final VisualSearchClient client;

    public VisualSearchStatusController(VisualSearchProperties properties, VisualSearchClient client) {
        this.properties = properties;
        this.client = client;
    }

    @GetMapping("/status")
    public ApiResponse<StatusResponse> status() {
        boolean enabled = properties.enabled();
        return ApiResponse.ok(new StatusResponse(enabled, enabled && client.isReady()));
    }

    public record StatusResponse(boolean enabled, boolean available) {}
}
