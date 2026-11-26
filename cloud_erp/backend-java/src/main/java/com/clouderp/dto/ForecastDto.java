package com.clouderp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ForecastDto {

    public static class Response {
        @JsonProperty("company_id")
        private Long companyId;

        private String company;

        @JsonProperty("generated_at")
        private String generatedAt;

        @JsonProperty("history_months")
        private List<MonthLabel> historyMonths;

        @JsonProperty("forecast_months")
        private List<MonthLabel> forecastMonths;

        private Summary summary;
        private List<ProductForecast> forecasts;

        public Long getCompanyId() { return companyId; }
        public void setCompanyId(Long companyId) { this.companyId = companyId; }

        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }

        public String getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }

        public List<MonthLabel> getHistoryMonths() { return historyMonths; }
        public void setHistoryMonths(List<MonthLabel> historyMonths) { this.historyMonths = historyMonths; }

        public List<MonthLabel> getForecastMonths() { return forecastMonths; }
        public void setForecastMonths(List<MonthLabel> forecastMonths) { this.forecastMonths = forecastMonths; }

        public Summary getSummary() { return summary; }
        public void setSummary(Summary summary) { this.summary = summary; }

        public List<ProductForecast> getForecasts() { return forecasts; }
        public void setForecasts(List<ProductForecast> forecasts) { this.forecasts = forecasts; }
    }

    public static class MonthLabel {
        private String month;
        private String label;

        public MonthLabel(String month, String label) {
            this.month = month;
            this.label = label;
        }

        public String getMonth() { return month; }
        public String getLabel() { return label; }
    }

    public static class Summary {
        @JsonProperty("products_analyzed")
        private Integer productsAnalyzed;

        @JsonProperty("high_risk_products")
        private Integer highRiskProducts;

        @JsonProperty("medium_risk_products")
        private Integer mediumRiskProducts;

        @JsonProperty("three_month_forecast_units")
        private Integer threeMonthForecastUnits;

        public Summary(Integer productsAnalyzed, Integer highRiskProducts, Integer mediumRiskProducts, Integer threeMonthForecastUnits) {
            this.productsAnalyzed = productsAnalyzed;
            this.highRiskProducts = highRiskProducts;
            this.mediumRiskProducts = mediumRiskProducts;
            this.threeMonthForecastUnits = threeMonthForecastUnits;
        }

        public Integer getProductsAnalyzed() { return productsAnalyzed; }
        public Integer getHighRiskProducts() { return highRiskProducts; }
        public Integer getMediumRiskProducts() { return mediumRiskProducts; }
        public Integer getThreeMonthForecastUnits() { return threeMonthForecastUnits; }
    }

    public static class MonthlyForecastItem {
        private String month;
        private String label;
        @JsonProperty("predicted_units")
        private Integer predictedUnits;

        public MonthlyForecastItem(String month, String label, Integer predictedUnits) {
            this.month = month;
            this.label = label;
            this.predictedUnits = predictedUnits;
        }

        public String getMonth() { return month; }
        public String getLabel() { return label; }
        public Integer getPredictedUnits() { return predictedUnits; }
    }

    public static class ProductForecast {
        @JsonProperty("product_id")
        private Long productId;
        private String product;
        @JsonProperty("current_stock")
        private Integer currentStock;
        @JsonProperty("reorder_level")
        private Integer reorderLevel;
        @JsonProperty("historical_units")
        private List<Integer> historicalUnits;
        @JsonProperty("forecast_method")
        private String forecastMethod;
        private List<MonthlyForecastItem> forecast;
        @JsonProperty("next_month_demand")
        private Integer nextMonthDemand;
        @JsonProperty("next_three_month_demand")
        private Integer nextThreeMonthDemand;
        @JsonProperty("stock_after_next_month")
        private Integer stockAfterNextMonth;
        @JsonProperty("stockout_risk")
        private String stockoutRisk;
        @JsonProperty("recommended_reorder")
        private Integer recommendedReorder;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProduct() { return product; }
        public void setProduct(String product) { this.product = product; }

        public Integer getCurrentStock() { return currentStock; }
        public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

        public Integer getReorderLevel() { return reorderLevel; }
        public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

        public List<Integer> getHistoricalUnits() { return historicalUnits; }
        public void setHistoricalUnits(List<Integer> historicalUnits) { this.historicalUnits = historicalUnits; }

        public String getForecastMethod() { return forecastMethod; }
        public void setForecastMethod(String forecastMethod) { this.forecastMethod = forecastMethod; }

        public List<MonthlyForecastItem> getForecast() { return forecast; }
        public void setForecast(List<MonthlyForecastItem> forecast) { this.forecast = forecast; }

        public Integer getNextMonthDemand() { return nextMonthDemand; }
        public void setNextMonthDemand(Integer nextMonthDemand) { this.nextMonthDemand = nextMonthDemand; }

        public Integer getNextThreeMonthDemand() { return nextThreeMonthDemand; }
        public void setNextThreeMonthDemand(Integer nextThreeMonthDemand) { this.nextThreeMonthDemand = nextThreeMonthDemand; }

        public Integer getStockAfterNextMonth() { return stockAfterNextMonth; }
        public void setStockAfterNextMonth(Integer stockAfterNextMonth) { this.stockAfterNextMonth = stockAfterNextMonth; }

        public String getStockoutRisk() { return stockoutRisk; }
        public void setStockoutRisk(String stockoutRisk) { this.stockoutRisk = stockoutRisk; }

        public Integer getRecommendedReorder() { return recommendedReorder; }
        public void setRecommendedReorder(Integer recommendedReorder) { this.recommendedReorder = recommendedReorder; }
    }
}
