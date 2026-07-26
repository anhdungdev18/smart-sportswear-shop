package com.dunghaiquyen.ecommerce.modules.replenishment.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReplenishmentSuggestionDetailResponse extends ReplenishmentSuggestionResponse {
    private int policyLeadTimeDays;
    private int policyTargetCoverDays;
    private double policyServiceLevel;
    private Map<String, Object> explanationJson;
    private List<DailyChartData> historyData;
    private List<DailyChartData> futureForecastData;
    private List<ModelMetric> modelMetrics;
    private String selectedModel;
    private String selectionReason;
    private String dataSource;
    private String demandPattern;
    private String forecastConfidence;
    private Double bias;
    private Double residualStdDev;
    private Integer backtestWindows;
    private Integer testWindowDays;
    private String trainingFrom;
    private String trainingTo;
    private String benchmarkAlgorithm;
    private Double benchmarkMae;
    private Double benchmarkWape;
    private String fallbackReason;

    @Data
    public static class DailyChartData {
        private String date;
        private Double actual;
        private Double forecast;
        private boolean backtestPeriod;

        public DailyChartData(String date, Double actual, Double forecast, boolean backtestPeriod) {
            this.date = date;
            this.actual = actual;
            this.forecast = forecast;
            this.backtestPeriod = backtestPeriod;
        }
    }

    @Data
    public static class ModelMetric {
        private String algorithm;
        private double mae;
        private Double wape;
        private Double bias;
        private Double residualStdDev;
        private boolean benchmark;
        private boolean selected;

        public ModelMetric(String algorithm, double mae, Double wape, boolean selected) {
            this.algorithm = algorithm;
            this.mae = mae;
            this.wape = wape;
            this.selected = selected;
        }

        public ModelMetric(String algorithm, double mae, Double wape, Double bias,
                           Double residualStdDev, boolean benchmark, boolean selected) {
            this.algorithm = algorithm;
            this.mae = mae;
            this.wape = wape;
            this.bias = bias;
            this.residualStdDev = residualStdDev;
            this.benchmark = benchmark;
            this.selected = selected;
        }
    }
}
