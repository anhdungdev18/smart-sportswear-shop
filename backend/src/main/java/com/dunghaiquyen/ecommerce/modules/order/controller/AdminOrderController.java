package com.dunghaiquyen.ecommerce.modules.order.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.order.dto.AdminOrderListQuery;
import com.dunghaiquyen.ecommerce.modules.order.dto.AdminOrderResponse;
import com.dunghaiquyen.ecommerce.modules.order.dto.UpdateOrderStatusRequest;
import com.dunghaiquyen.ecommerce.modules.order.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** API_SPEC_PHASE1.md section 10 - admin/sales order management. */
@RestController
@RequestMapping("/api/v1/admin/orders")
@PreAuthorize("hasAnyRole('ADMIN','SALES_STAFF')")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ApiResponse<List<AdminOrderResponse>> list(@ModelAttribute AdminOrderListQuery query) {
        OrderService.ListResult<AdminOrderResponse> result = orderService.listOrdersForAdmin(query);
        return ApiResponse.ok(result.items(), result.meta());
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminOrderResponse> detail(@PathVariable UUID id) {
        return ApiResponse.ok(orderService.getOrderDetailForAdmin(id));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<AdminOrderResponse> updateStatus(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        AdminOrderResponse response = orderService.updateOrderStatus(id, request, principal.getUser());
        return ApiResponse.ok("Order status updated", response);
    }
}
