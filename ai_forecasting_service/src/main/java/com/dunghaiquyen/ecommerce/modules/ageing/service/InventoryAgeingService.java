package com.dunghaiquyen.ecommerce.modules.ageing.service;

import com.dunghaiquyen.ecommerce.config.ForecastDataSourceProperties;
import com.dunghaiquyen.ecommerce.modules.ageing.dto.InventoryAgeingItemResponse;
import com.dunghaiquyen.ecommerce.modules.ageing.dto.InventoryAgeingStatus;
import com.dunghaiquyen.ecommerce.modules.ageing.dto.InventoryAgeingSummaryResponse;
import com.dunghaiquyen.ecommerce.modules.ageing.repository.InventoryAgeingRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryAgeingService {
    private final InventoryAgeingRepository repository;
    private final ForecastDataSourceProperties dataSourceProperties;

    public InventoryAgeingService(InventoryAgeingRepository repository, ForecastDataSourceProperties dataSourceProperties) {
        this.repository = repository;
        this.dataSourceProperties = dataSourceProperties;
    }

    @Transactional(readOnly = true)
    public InventoryAgeingSummaryResponse summarize(String requestedSource) {
        String source = requestedSource == null || requestedSource.isBlank()
                ? dataSourceProperties.dataSource() : requestedSource.toUpperCase();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        List<InventoryAgeingItemResponse> items = repository.findRows(source, today).stream()
                .filter(row -> row.availableQuantity() > 0)
                .map(row -> map(row, today))
                .sorted(java.util.Comparator.comparingInt(InventoryAgeingItemResponse::urgencyScore).reversed())
                .toList();
        BigDecimal atRisk = items.stream()
                .filter(item -> item.status() == InventoryAgeingStatus.SLOW_MOVING
                        || item.status() == InventoryAgeingStatus.DORMANT
                        || item.status() == InventoryAgeingStatus.DEAD_STOCK)
                .map(InventoryAgeingItemResponse::estimatedInventoryValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new InventoryAgeingSummaryResponse(source, Instant.now(), items.size(), items.size(),
                count(items, InventoryAgeingStatus.NEW_NO_SALES), count(items, InventoryAgeingStatus.WATCH),
                count(items, InventoryAgeingStatus.SLOW_MOVING), count(items, InventoryAgeingStatus.DORMANT),
                count(items, InventoryAgeingStatus.DEAD_STOCK),
                (int) items.stream().filter(item -> !item.supplierConfigured()).count(), atRisk, items);
    }

    private InventoryAgeingItemResponse map(InventoryAgeingRepository.AgeingRow row, LocalDate today) {
        int age = (int) Math.max(0, ChronoUnit.DAYS.between(row.stockStartDate(), today));
        int noSaleDays = row.lastSaleDate() == null ? age : (int) Math.max(0, ChronoUnit.DAYS.between(row.lastSaleDate(), today));
        InventoryAgeingStatus status = status(noSaleDays);
        int urgency = Math.min(100, noSaleDays * 45 / 180 + Math.min(25, row.availableQuantity())
                + Math.min(20, row.unitPrice().multiply(BigDecimal.valueOf(row.availableQuantity())).divide(BigDecimal.valueOf(500_000), 0, java.math.RoundingMode.DOWN).intValue()));
        return new InventoryAgeingItemResponse(row.variantId(), row.productId(), row.sku(), row.productName(),
                row.size(), row.color(), row.availableQuantity(), row.unitPrice(),
                row.unitPrice().multiply(BigDecimal.valueOf(row.availableQuantity())), row.stockStartDate(), row.lastSaleDate(),
                age, noSaleDays, row.sold30(), row.sold90(), row.sold180(), status, urgency,
                row.supplierConfigured(), actions(status));
    }

    private InventoryAgeingStatus status(int days) {
        if (days < 30) return InventoryAgeingStatus.NEW_NO_SALES;
        if (days < 60) return InventoryAgeingStatus.WATCH;
        if (days < 90) return InventoryAgeingStatus.SLOW_MOVING;
        if (days < 180) return InventoryAgeingStatus.DORMANT;
        return InventoryAgeingStatus.DEAD_STOCK;
    }

    private List<String> actions(InventoryAgeingStatus status) {
        return switch (status) {
            case NEW_NO_SALES -> List.of("Kiểm tra trạng thái hiển thị, hình ảnh, nội dung và giá bán.");
            case WATCH -> List.of("Tăng hiển thị và thử bán kèm với sản phẩm bán chạy.");
            case SLOW_MOVING -> List.of("Dừng nhập thêm.", "Cân nhắc combo hoặc ưu đãi nhẹ 5-10%.");
            case DORMANT -> List.of("Khóa đề xuất nhập.", "Cân nhắc giảm 10-20% hoặc điều chuyển kho.");
            case DEAD_STOCK -> List.of("Ưu tiên thanh lý hoặc bán theo lô.", "Xem xét ngừng kinh doanh SKU sau khi hết tồn.");
        };
    }

    private int count(List<InventoryAgeingItemResponse> items, InventoryAgeingStatus status) {
        return (int) items.stream().filter(item -> item.status() == status).count();
    }
}
