package com.dunghaiquyen.ecommerce.modules.report.controller;

import com.dunghaiquyen.ecommerce.common.response.ApiResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.InventoryReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.CustomerReportQuery;
import com.dunghaiquyen.ecommerce.modules.report.dto.CustomerReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.OrderReportQuery;
import com.dunghaiquyen.ecommerce.modules.report.dto.OrderReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.OverviewReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.ProductReportQuery;
import com.dunghaiquyen.ecommerce.modules.report.dto.ProductReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.RevenueReportQuery;
import com.dunghaiquyen.ecommerce.modules.report.dto.RevenueReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/orders")
    public ApiResponse<OrderReportResponse> orders(@ModelAttribute OrderReportQuery query) {
        return ApiResponse.ok(reportService.getOrderReport(query));
    }

    @GetMapping("/products")
    public ApiResponse<ProductReportResponse> products(@ModelAttribute ProductReportQuery query) {
        return ApiResponse.ok(reportService.getProductReport(query));
    }

    @GetMapping("/customers")
    public ApiResponse<CustomerReportResponse> customers(@ModelAttribute CustomerReportQuery query) {
        return ApiResponse.ok(reportService.getCustomerReport(query));
    }

    @GetMapping("/inventory")
    public ApiResponse<InventoryReportResponse> inventory() {
        return ApiResponse.ok(reportService.getInventoryReport());
    }
}
