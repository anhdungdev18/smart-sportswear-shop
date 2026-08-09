package com.dunghaiquyen.ecommerce.modules.order.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.common.security.CustomUserDetails;
import com.dunghaiquyen.ecommerce.modules.order.dto.AdminOrderListQuery;
import com.dunghaiquyen.ecommerce.modules.order.dto.AdminOrderResponse;
import com.dunghaiquyen.ecommerce.modules.order.dto.UpdateOrderStatusRequest;
import com.dunghaiquyen.ecommerce.modules.order.dto.CancelOrderRequest;
import com.dunghaiquyen.ecommerce.modules.order.service.OrderService;
import com.dunghaiquyen.ecommerce.modules.returns.dto.RefundResponse;
import com.dunghaiquyen.ecommerce.modules.returns.service.ReturnService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    private final ReturnService returnService;

    public AdminOrderController(OrderService orderService, ReturnService returnService) {
        this.orderService = orderService;
        this.returnService = returnService;
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

    @GetMapping("/{id}/refunds")
    public ApiResponse<List<RefundResponse>> refunds(@PathVariable UUID id) {
        return ApiResponse.ok(returnService.listOrderRefundsForAdmin(id));
    }

    @PostMapping("/{id}/cancellation-refund")
    public ApiResponse<RefundResponse> refundCancellation(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            HttpServletRequest request) {
        // This transaction commits before the external gateway call below, so
        // a VNPay outage cannot erase the admin's approval decision.
        orderService.approveCancellationForRefund(id, principal.getUser());
        String ipAddress = request.getRemoteAddr();
        RefundResponse response = returnService.refundCancellation(
                id, "Approved cancellation request", principal.getUser(), ipAddress);
        return ApiResponse.ok("Cancellation refund submitted", response);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<AdminOrderResponse> cancelByStaff(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @RequestBody(required = false) CancelOrderRequest request) {
        String reason = request == null ? null : request.reason();
        AdminOrderResponse response = orderService.cancelOrderByStaff(id, reason, principal.getUser());
        String message = response.orderStatus() == com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus.CANCELLATION_REQUESTED
                ? "Staff cancellation awaiting refund" : "Order cancelled by staff";
        return ApiResponse.ok(message, response);
    }

    @PostMapping("/{id}/cancellation-rejection")
    public ApiResponse<AdminOrderResponse> rejectCancellation(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable UUID id,
            @RequestBody(required = false) CancelOrderRequest request) {
        returnService.rejectCancellationRequest(id, request == null ? null : request.reason(), principal.getUser());
        return ApiResponse.ok("Cancellation request rejected", orderService.getOrderDetailForAdmin(id));
    }
}
