package com.dunghaiquyen.ecommerce.modules.order.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.order.dto.CancelOrderRequest;
import com.dunghaiquyen.ecommerce.modules.order.dto.CreateOrderRequest;
import com.dunghaiquyen.ecommerce.modules.order.dto.OrderListQuery;
import com.dunghaiquyen.ecommerce.modules.order.dto.OrderResponse;
import com.dunghaiquyen.ecommerce.modules.order.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** API_SPEC_PHASE1.md section 7 - checkout and the customer's own order history. */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrderFromCart(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Order created", response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<List<OrderResponse>> listMine(
            @AuthenticationPrincipal CustomUserDetails principal, @ModelAttribute OrderListQuery query) {
        OrderService.ListResult<OrderResponse> result = orderService.listMyOrders(principal.getUserId(), query);
        return ApiResponse.ok(result.items(), result.meta());
    }

    /**
     * Shared across CUSTOMER/ADMIN/SALES_STAFF per the spec (7.3): a customer
     * can only see their own order (404 otherwise, ownership enforced in the
     * service), admin/sales can see any order. Always returns the
     * customer-safe shape (no internalNote) - the fuller staff view lives at
     * GET /api/v1/admin/orders/:id.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','SALES_STAFF')")
    public ApiResponse<OrderResponse> detail(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable UUID id) {
        OrderResponse response =
                orderService.getOrderDetail(id, principal.getUserId(), principal.getUser().getRole());
        return ApiResponse.ok(response);
    }

    /**
     * API_SPEC_PHASE1.md 7.4: only the order's own customer can cancel it, and
     * only while it is still PENDING_CONFIRMATION (see OrderService.cancelOwnOrder
     * for why - same rule the admin status flow already enforces for CANCELLED).
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<OrderResponse> cancel(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @RequestBody(required = false) CancelOrderRequest request) {
        String reason = request != null ? request.reason() : null;
        OrderResponse response = orderService.cancelOwnOrder(id, principal.getUserId(), reason);
        return ApiResponse.ok("Order cancelled", response);
    }
}
