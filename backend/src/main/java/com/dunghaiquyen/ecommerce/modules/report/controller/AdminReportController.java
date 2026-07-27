package com.dunghaiquyen.ecommerce.modules.report.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.InventoryReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.InventoryLookupItemResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.BestSellingProductPeriodResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.OrderReportQuery;
import com.dunghaiquyen.ecommerce.modules.report.dto.OrderReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.OrderStatusTrendResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.OverviewReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.ProductReportQuery;
import com.dunghaiquyen.ecommerce.modules.report.dto.ProductReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.RevenueBreakdownResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.RevenueReportQuery;
import com.dunghaiquyen.ecommerce.modules.report.dto.RevenueReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** API_SPEC_PHASE1.md section 12 - admin dashboard/report APIs. */
@RestController
@RequestMapping("/api/v1/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/overview")
    public ApiResponse<OverviewReportResponse> overview() {
        return ApiResponse.ok(reportService.getOverview());
    }

    @GetMapping("/revenue")
    public ApiResponse<RevenueReportResponse> revenue(@ModelAttribute RevenueReportQuery query) {
        return ApiResponse.ok(reportService.getRevenueReport(query));
    }

    @GetMapping("/revenue/breakdown")
    public ApiResponse<RevenueBreakdownResponse> revenueBreakdown() {
        return ApiResponse.ok(reportService.getRevenueBreakdown());
    }

    @GetMapping("/orders")
    public ApiResponse<OrderReportResponse> orders(@ModelAttribute OrderReportQuery query) {
        return ApiResponse.ok(reportService.getOrderReport(query));
    }

    @GetMapping("/orders/status-trend")
    public ApiResponse<OrderStatusTrendResponse> orderStatusTrend(
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {
        return ApiResponse.ok(reportService.getOrderStatusTrend(dateFrom, dateTo));
    }

    @GetMapping("/products")
    public ApiResponse<ProductReportResponse> products(@ModelAttribute ProductReportQuery query) {
        return ApiResponse.ok(reportService.getProductReport(query));
    }

    @GetMapping("/inventory")
    public ApiResponse<InventoryReportResponse> inventory() {
        return ApiResponse.ok(reportService.getInventoryReport());
    }

    @GetMapping("/inventory/lookup")
    public ApiResponse<List<InventoryLookupItemResponse>> inventoryLookup(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) UUID variantId,
            @RequestParam(defaultValue = "20") Integer limit) {
        return ApiResponse.ok(reportService.lookupInventory(query, sku, variantId, limit));
    }

    @GetMapping("/products/best-sellers")
    public ApiResponse<BestSellingProductPeriodResponse> bestSellers(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "10") Integer limit) {
        return ApiResponse.ok(reportService.getBestSellers(fromDate, toDate, limit));
    }
}
