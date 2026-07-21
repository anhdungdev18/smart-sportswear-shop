package com.dunghaiquyen.ecommerce.modules.replenishment.service;

import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ForecastRun;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.InventoryPolicy;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentPriority;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentRecommendation;
import com.dunghaiquyen.ecommerce.modules.replenishment.entity.ReplenishmentStatus;
import com.dunghaiquyen.ecommerce.modules.replenishment.repository.VariantSnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ReplenishmentService {

    private final ObjectMapper objectMapper;

    public ReplenishmentService(ObjectMapper objectMapper) {
        this. objectMapper = objectMapper;
    }

    public ReplenishmentRecommendation generateRecommendation(
            VariantSnapshot variant,
            ForecastRun forecastRun,
            InventoryPolicy policy) {

        int availableQuantity = variant.availableQuantity();
        int incomingQuantity = 0; // Not implemented in MVP

        double averageDailyDemand = forecastRun.getAverageDailyDemand().doubleValue();
        int leadTimeDays = policy.getLeadTimeDays();
        int targetCoverDays = policy.getTargetCoverDays();
        int minOrder = policy.getMinimumOrderQuantity();
        int packSize = policy.getPackSize();

        int safetyStock;
        if (forecastRun.getResidualStdDev() != null) {
            double z = getZScore(policy.getServiceLevel());
            double stdDev = forecastRun.getResidualStdDev().doubleValue();
            safetyStock = (int) Math.ceil(z * stdDev * Math.sqrt(leadTimeDays));
        } else {
            safetyStock = (int) Math.ceil(averageDailyDemand * 3);
        }

        int reorderPoint = (int) Math.ceil(averageDailyDemand * leadTimeDays + safetyStock);
        int targetStock = (int) Math.ceil(averageDailyDemand * (leadTimeDays + targetCoverDays) + safetyStock);
        
        int rawSuggestion = Math.max(0, targetStock - availableQuantity - incomingQuantity);
        if (rawSuggestion > 0) {
            rawSuggestion = Math.max(rawSuggestion, minOrder);
        }

        int suggestedQuantity = (int) (Math.ceil((double) rawSuggestion / packSize) * packSize);

        Integer estimatedStockoutDays = null;
        if (averageDailyDemand > 0) {
            estimatedStockoutDays = (int) Math.floor(availableQuantity / averageDailyDemand);
        }

        ReplenishmentPriority priority = determinePriority(availableQuantity, estimatedStockoutDays, leadTimeDays, reorderPoint, suggestedQuantity);

        ReplenishmentRecommendation rec = new ReplenishmentRecommendation();
        rec.setVariantId(variant.id());
        rec.setForecastRun(forecastRun);
        rec.setAvailableQuantity(availableQuantity);
        rec.setIncomingQuantity(incomingQuantity);
        rec.setReorderPoint(reorderPoint);
        rec.setSafetyStock(safetyStock);
        rec.setSuggestedQuantity(suggestedQuantity);
        rec.setEstimatedStockoutDays(estimatedStockoutDays);
        rec.setPriority(priority);
        rec.setStatus(ReplenishmentStatus.PENDING);
        rec.setExplanation(buildExplanationMap(availableQuantity, averageDailyDemand, estimatedStockoutDays, leadTimeDays, packSize, safetyStock, reorderPoint, targetStock, rawSuggestion, suggestedQuantity));

        return rec;
    }

    private double getZScore(BigDecimal serviceLevel) {
        double val = serviceLevel.doubleValue();
        if (val >= 0.99) return 2.33;
        if (val >= 0.975) return 1.96;
        if (val >= 0.95) return 1.65;
        if (val >= 0.90) return 1.28;
        return 1.65; // default 95%
    }

    private ReplenishmentPriority determinePriority(int availableQuantity, Integer stockoutDays, int leadTime, int reorderPoint, int suggestedQuantity) {
        if (availableQuantity == 0 || (stockoutDays != null && stockoutDays <= leadTime)) {
            return ReplenishmentPriority.CRITICAL;
        }
        if (stockoutDays != null && stockoutDays <= leadTime + 7) {
            return ReplenishmentPriority.HIGH;
        }
        if (availableQuantity <= reorderPoint) {
            return ReplenishmentPriority.MEDIUM;
        }
        return ReplenishmentPriority.LOW;
    }

    private Map<String, Object> buildExplanationMap(int available, double avgDemand, Integer stockoutDays, int leadTime, int packSize, int safetyStock, int reorderPoint, int targetStock, int rawSuggestion, int roundedSuggestion) {
        Map<String, Object> root = new HashMap<>();
        
        // Build summary
        String summary;
        if (available == 0) {
            summary = "SKU đã hết hàng.";
        } else if (stockoutDays != null && stockoutDays <= leadTime) {
            summary = "SKU dự kiến hết hàng trước khi lô hàng mới có thể về.";
        } else if (available <= reorderPoint) {
            summary = "SKU đã chạm mức đặt hàng lại (Reorder Point).";
        } else {
            summary = "SKU vẫn còn đủ hàng trong thời gian tới.";
        }
        root.put("summary", summary);

        // Build reasons
        List<String> reasons = new ArrayList<>();
        reasons.add(String.format("Tồn khả dụng hiện tại là %d sản phẩm.", available));
        reasons.add(String.format("Nhu cầu trung bình dự báo là %.1f sản phẩm/ngày.", avgDemand));
        if (stockoutDays != null) {
            reasons.add(String.format("Tồn kho dự kiến chỉ đủ khoảng %d ngày.", stockoutDays));
        }
        reasons.add(String.format("Thời gian nhập hàng được cấu hình là %d ngày.", leadTime));
        if (roundedSuggestion > rawSuggestion) {
            reasons.add(String.format("Đề xuất đã được làm tròn theo quy cách đóng gói %d sản phẩm.", packSize));
        }
        root.put("reasons", reasons);

        // Build formula
        Map<String, Integer> formula = new HashMap<>();
        formula.put("safetyStock", safetyStock);
        formula.put("reorderPoint", reorderPoint);
        formula.put("targetStock", targetStock);
        formula.put("rawSuggestion", rawSuggestion);
        formula.put("roundedSuggestion", roundedSuggestion);
        root.put("formula", formula);

        return root;
    }
}
