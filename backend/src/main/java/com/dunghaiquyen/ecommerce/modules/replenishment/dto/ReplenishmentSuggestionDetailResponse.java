package com.dunghaiquyen.ecommerce.modules.replenishment.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReplenishmentSuggestionDetailResponse extends ReplenishmentSuggestionResponse {
    
    // policy snapshot
    private int policyLeadTimeDays;
    private int policyTargetCoverDays;
    private double policyServiceLevel;
    
    // explanation
    private Map<String, Object> explanationJson;
    
    // charts data
    private List<DailyChartData> historyData;
    private List<DailyChartData> futureForecastData;

    @Data
    public static class DailyChartData {
        private String date;
        private Double actual;
        private Double forecast;
        
        public DailyChartData(String date, Double actual, Double forecast) {
            this.date = date;
            this.actual = actual;
            this.forecast = forecast;
        }
    }
}
