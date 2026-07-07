package com.dunghaiquyen.ecommerce.modules.report.service;

import com.dunghaiquyen.ecommerce.common.time.AppTimeZone;
import com.dunghaiquyen.ecommerce.config.AppReportProperties;
import com.dunghaiquyen.ecommerce.config.CacheConfig;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.payment.entity.PaymentStatus;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus;
import com.dunghaiquyen.ecommerce.modules.report.dto.BestSellingProductResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.InventoryReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.LowStockItemResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.OrderReportQuery;
import com.dunghaiquyen.ecommerce.modules.report.dto.OrderReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.OrderStatusCount;
import com.dunghaiquyen.ecommerce.modules.report.dto.OverviewReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.ProductReportQuery;
import com.dunghaiquyen.ecommerce.modules.report.dto.ProductReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.repository.OrderItemReportRepository;
import com.dunghaiquyen.ecommerce.modules.report.repository.OrderReportRepository;
import com.dunghaiquyen.ecommerce.modules.report.repository.ProductVariantReportRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Revenue rule (PHASE1_SPEC.md 6.9, locked, not invented here):
 * - grossRevenue = SUM(totalAmount) over every order with paymentStatus = PAID,
 *   regardless of orderStatus. The Phase H special combo (a success callback
 *   arriving AFTER an order was independently cancelled) is already safe here
 *   by construction, not by anything added in this phase: PaymentService's
 *   callback handler deliberately never writes Order.paymentStatus once
 *   orderStatus is CANCELLED (only Payment.status reflects PAID in that case),
 *   so a cancelled order can never read back as paymentStatus = PAID and can
 *   never be picked up by this query. Covered by a regression test below.
 * - realizedRevenue = SUM(totalAmount) over every order with orderStatus =
 *   DELIVERED, regardless of paymentStatus (so a delivered COD order counts
 *   even though COD never flips paymentStatus to PAID in this phase - that is
 *   the literal spec rule, not a gap).
 * - Both numbers are shown together on the overview on purpose (spec: "tránh
 *   hiểu sai doanh thu") - they answer different questions (cash received vs.
 *   orders fully completed) and are not meant to reconcile to each other.
 */
@Service
public class ReportService {

    private static final int DEFAULT_PRODUCT_LIMIT = 10;
    private static final int MAX_PRODUCT_LIMIT = 50;
    private static final int MAX_LOW_STOCK_ITEMS = 50;

    /**
     * Sentinel bounds used when dateFrom/dateTo is omitted - resolving "no
     * filter" to a concrete far-past/far-future Instant (rather than passing a
     * literal null into the query) avoids Postgres failing to infer that bind
     * parameter's type for a column it's only ever compared against, never
     * checked for IS NULL, inside the query itself. Year 9999 stays safely
     * inside timestamptz's range (~294276 AD) with room to spare.
     */
    private static final Instant FAR_PAST = Instant.EPOCH;
    private static final Instant FAR_FUTURE =
            LocalDate.of(9999, 12, 31).atTime(LocalTime.MAX).atZone(AppTimeZone.ZONE).toInstant();

    private final OrderReportRepository orderReportRepository;
    private final OrderItemReportRepository orderItemReportRepository;
    private final ProductVariantReportRepository productVariantReportRepository;
    private final AppReportProperties reportProperties;

    public ReportService(
            OrderReportRepository orderReportRepository,
            OrderItemReportRepository orderItemReportRepository,
            ProductVariantReportRepository productVariantReportRepository,
            AppReportProperties reportProperties) {
        this.orderReportRepository = orderReportRepository;
        this.orderItemReportRepository = orderItemReportRepository;
        this.productVariantReportRepository = productVariantReportRepository;
        this.reportProperties = reportProperties;
    }

    @Cacheable(CacheConfig.REPORT_OVERVIEW)
    @Transactional(readOnly = true)
    public OverviewReportResponse getOverview() {
        var grossRevenue = orderReportRepository.sumTotalAmountByPaymentStatus(PaymentStatus.PAID);
        var realizedRevenue = orderReportRepository.sumTotalAmountByOrderStatus(OrderStatus.DELIVERED);
        long totalOrders = orderReportRepository.count();
        long pendingOrders = orderReportRepository.countByOrderStatus(OrderStatus.PENDING_CONFIRMATION);
        long lowStockCount = productVariantReportRepository.countLowStock(VariantStatus.INACTIVE, lowStockThreshold());
        return new OverviewReportResponse(grossRevenue, realizedRevenue, totalOrders, pendingOrders, lowStockCount);
    }

    @Transactional(readOnly = true)
    public OrderReportResponse getOrderReport(OrderReportQuery query) {
        LocalDate dateFrom = query.dateFrom();
        LocalDate dateTo = query.dateTo();
        Instant from = dateFrom != null ? dateFrom.atStartOfDay(AppTimeZone.ZONE).toInstant() : FAR_PAST;
        Instant to = dateTo != null ? dateTo.atTime(LocalTime.MAX).atZone(AppTimeZone.ZONE).toInstant() : FAR_FUTURE;
        List<OrderStatusCount> byStatus = orderReportRepository.countByStatusInRange(from, to);
        long totalOrders = byStatus.stream().mapToLong(OrderStatusCount::count).sum();
        return new OrderReportResponse(dateFrom, dateTo, totalOrders, byStatus);
    }

    @Cacheable(value = CacheConfig.REPORT_PRODUCTS, key = "#query.limit() != null ? #query.limit() : 'default'")
    @Transactional(readOnly = true)
    public ProductReportResponse getProductReport(ProductReportQuery query) {
        List<BestSellingProductResponse> bestSelling = orderItemReportRepository.findBestSelling(
                OrderStatus.CANCELLED, PageRequest.of(0, resolveProductLimit(query.limit())));
        return new ProductReportResponse(bestSelling);
    }

    @Cacheable(CacheConfig.REPORT_INVENTORY)
    @Transactional(readOnly = true)
    public InventoryReportResponse getInventoryReport() {
        long totalVariants = productVariantReportRepository.count();
        long totalStock = productVariantReportRepository.sumStockQuantity();
        long totalReserved = productVariantReportRepository.sumReservedQuantity();
        int threshold = lowStockThreshold();
        long lowStockCount = productVariantReportRepository.countLowStock(VariantStatus.INACTIVE, threshold);
        List<LowStockItemResponse> items = productVariantReportRepository
                .findLowStockVariants(VariantStatus.INACTIVE, threshold, PageRequest.of(0, MAX_LOW_STOCK_ITEMS))
                .stream()
                .map(this::toLowStockItem)
                .toList();
        return new InventoryReportResponse(
                totalVariants, totalStock, totalReserved, totalStock - totalReserved, threshold, lowStockCount, items);
    }

    private LowStockItemResponse toLowStockItem(ProductVariant variant) {
        return new LowStockItemResponse(
                variant.getId(),
                variant.getSku(),
                variant.getProduct().getName(),
                variant.getStockQuantity(),
                variant.getReservedQuantity(),
                variant.getStockQuantity() - variant.getReservedQuantity());
    }

    private int lowStockThreshold() {
        Integer configured = reportProperties.lowStockThreshold();
        return configured != null ? configured : 10;
    }

    private int resolveProductLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_PRODUCT_LIMIT;
        }
        return Math.min(limit, MAX_PRODUCT_LIMIT);
    }
}
