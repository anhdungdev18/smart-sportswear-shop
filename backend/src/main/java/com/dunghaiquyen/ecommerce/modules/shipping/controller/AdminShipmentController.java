package com.dunghaiquyen.ecommerce.modules.shipping.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.shipping.dto.ShipmentResponse;
import com.dunghaiquyen.ecommerce.modules.shipping.dto.UpdateShipmentRequest;
import com.dunghaiquyen.ecommerce.modules.shipping.service.ShipmentService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Nested under the same /api/v1/admin/orders path AdminOrderController owns,
 * but kept in its own controller/module (package-by-feature) rather than
 * added to AdminOrderController directly - mirrors how ReviewController
 * already lives under the review module while mapped onto a product-shaped
 * path. Role check is hasAnyRole('ADMIN','WAREHOUSE_STAFF'), matching
 * InventoryController: tracking number/carrier/shipment status are
 * warehouse/fulfillment duties, not the sales-facing order-status concern
 * AdminOrderController's own SALES_STAFF access covers.
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
@PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_STAFF')")
public class AdminShipmentController {

    private final ShipmentService shipmentService;

    public AdminShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping("/{id}/shipping")
    public ApiResponse<ShipmentResponse> detail(@PathVariable UUID id) {
        return ApiResponse.ok(shipmentService.getByOrderId(id));
    }

    @PatchMapping("/{id}/shipping")
    public ApiResponse<ShipmentResponse> update(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateShipmentRequest request) {
        ShipmentResponse response = shipmentService.upsertShipment(id, request, principal.getUserId());
        return ApiResponse.ok("Shipment updated", response);
    }
}
