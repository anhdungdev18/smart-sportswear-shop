package com.dunghaiquyen.ecommerce.modules.report.service;

import com.dunghaiquyen.ecommerce.common.time.AppTimeZone;
import com.dunghaiquyen.ecommerce.config.AppReportProperties;
import com.dunghaiquyen.ecommerce.config.CacheConfig;
import com.dunghaiquyen.ecommerce.modules.order.entity.OrderStatus;
import com.dunghaiquyen.ecommerce.modules.payment.entity.PaymentStatus;
import com.dunghaiquyen.ecommerce.modules.product.entity.ProductVariant;
import com.dunghaiquyen.ecommerce.modules.product.entity.VariantStatus;
import com.dunghaiquyen.ecommerce.modules.report.dto.BestSellingProductResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.BestSellingProductPeriodResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.InventoryLookupItemResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.InventoryReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.LowStockItemResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.OrderReportQuery;
import com.dunghaiquyen.ecommerce.modules.report.dto.OrderReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.OrderStatusCount;
import com.dunghaiquyen.ecommerce.modules.report.dto.OverviewReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.ProductReportQuery;
import com.dunghaiquyen.ecommerce.modules.report.dto.ProductReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.RevenueBucketRow;
import com.dunghaiquyen.ecommerce.modules.report.dto.RevenueGranularity;
import com.dunghaiquyen.ecommerce.modules.report.dto.RevenuePointResponse;
import com.dunghaiquyen.ecommerce.modules.report.dto.RevenueReportQuery;
import com.dunghaiquyen.ecommerce.modules.report.dto.RevenueReportResponse;
import com.dunghaiquyen.ecommerce.modules.report.repository.OrderItemReportRepository;
import com.dunghaiquyen.ecommerce.modules.report.repository.OrderReportRepository;
import com.dunghaiquyen.ecommerce.modules.report.repository.ProductVariantReportRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
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

    /** Safety cap so a wide custom range at DAY granularity can't produce a runaway series. */
    private static final int MAX_REVENUE_BUCKETS = 366;
    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MM/yyyy");

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

    /**
     * Gross-revenue time series (PAID orders) bucketed by day/month/year. When
     * dateFrom/dateTo are omitted a window is derived from the granularity (last
     * 30 days / 12 months / 5 years). Buckets with no PAID orders are emitted as
     * zero so the chart draws a continuous line rather than skipping gaps.
     */
    @Transactional(readOnly = true)
    public RevenueReportResponse getRevenueReport(RevenueReportQuery query) {
        RevenueGranularity granularity = RevenueGranularity.from(query.granularity());
        LocalDate today = LocalDate.now(AppTimeZone.ZONE);
        LocalDate to = query.dateTo() != null ? query.dateTo() : today;
        LocalDate from = query.dateFrom() != null ? query.dateFrom() : defaultFrom(granularity, to);
        if (from.isAfter(to)) {
            LocalDate swap = from;
            from = to;
            to = swap;
        }

        LocalDate firstBucket = truncate(granularity, from);
        LocalDate lastBucket = truncate(granularity, to);
        Instant fromInstant = from.atStartOfDay(AppTimeZone.ZONE).toInstant();
        Instant toInstant = to.atTime(LocalTime.MAX).atZone(AppTimeZone.ZONE).toInstant();

        Map<LocalDate, RevenueBucketRow> byBucket = orderReportRepository
                .sumRevenueByBucket(granularity.sqlField(), fromInstant, toInstant).stream()
                .collect(Collectors.toMap(RevenueBucketRow::getBucket, Function.identity(), (a, b) -> a));

        List<RevenuePointResponse> points = new ArrayList<>();
        LocalDate cursor = firstBucket;
        int guard = 0;
        while (!cursor.isAfter(lastBucket) && guard < MAX_REVENUE_BUCKETS) {
            RevenueBucketRow row = byBucket.get(cursor);
            BigDecimal revenue = row != null ? row.getRevenue() : BigDecimal.ZERO;
            long orders = row != null ? row.getOrderCount() : 0L;
            points.add(new RevenuePointResponse(label(granularity, cursor), cursor, revenue, orders));
            cursor = step(granularity, cursor);
            guard++;
        }
        return new RevenueReportResponse(granularity.name(), from, to, points);
    }

    private LocalDate defaultFrom(RevenueGranularity granularity, LocalDate to) {
        return switch (granularity) {
            case DAY -> to.minusDays(29);
            case MONTH -> to.minusMonths(11).withDayOfMonth(1);
            case YEAR -> to.minusYears(4).withDayOfYear(1);
        };
    }

    private LocalDate truncate(RevenueGranularity granularity, LocalDate date) {
        return switch (granularity) {
            case DAY -> date;
            case MONTH -> date.withDayOfMonth(1);
            case YEAR -> date.withDayOfYear(1);
        };
    }

    private LocalDate step(RevenueGranularity granularity, LocalDate date) {
        return switch (granularity) {
            case DAY -> date.plusDays(1);
            case MONTH -> date.plusMonths(1);
            case YEAR -> date.plusYears(1);
        };
    }

    private String label(RevenueGranularity granularity, LocalDate date) {
        return switch (granularity) {
            case DAY -> date.format(DAY_LABEL);
            case MONTH -> date.format(MONTH_LABEL);
            case YEAR -> String.valueOf(date.getYear());
        };
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

    @Transactional(readOnly = true)
    public List<InventoryLookupItemResponse> lookupInventory(String query, String sku, UUID variantId, Integer limit) {
        int resolvedLimit = resolveProductLimit(limit);
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        String normalizedSku = sku == null || sku.isBlank() ? null : sku.trim();
        List<ProductVariant> variants;
        if (variantId != null) {
            variants = productVariantReportRepository.findById(variantId).stream().toList();
        } else if (normalizedSku != null) {
            variants = productVariantReportRepository.findLookupBySku(normalizedSku);
        } else if (normalizedQuery != null) {
            variants = productVariantReportRepository.searchInventoryLookupByQuery(
                    normalizedQuery, PageRequest.of(0, resolvedLimit));
        } else {
            variants = List.of();
        }
        return variants.stream()
                .map(variant -> new InventoryLookupItemResponse(
                        variant.getId(),
                        variant.getProduct().getId(),
                        variant.getProduct().getName(),
                        variant.getProduct().getSlug(),
                        variant.getSku(),
                        variant.getSize(),
                        variant.getColor(),
                        variant.getStockQuantity(),
                        variant.getReservedQuantity(),
                        variant.getStockQuantity() - variant.getReservedQuantity(),
                        variant.getStatus(),
                        variant.getUpdatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public BestSellingProductPeriodResponse getBestSellers(LocalDate fromDate, LocalDate toDate, Integer limit) {
        LocalDate today = LocalDate.now(AppTimeZone.ZONE);
        LocalDate to = toDate != null ? toDate : today;
        LocalDate from = fromDate != null ? fromDate : to.minusDays(29);
        if (from.isAfter(to)) {
            LocalDate swap = from;
            from = to;
            to = swap;
        }
        int resolvedLimit = resolveProductLimit(limit);
        Instant fromInstant = from.atStartOfDay(AppTimeZone.ZONE).toInstant();
        Instant toInstant = to.atTime(LocalTime.MAX).atZone(AppTimeZone.ZONE).toInstant();
        List<BestSellingProductResponse> items = orderItemReportRepository.findBestSellingInRange(
                OrderStatus.CANCELLED, fromInstant, toInstant, PageRequest.of(0, resolvedLimit));
        return new BestSellingProductPeriodResponse(from, to, resolvedLimit, "order_items_excluding_cancelled", items);
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
