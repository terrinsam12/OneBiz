package com.clouderp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class AnalyticsDto {

    public static class OverviewResponse {
        @JsonProperty("company_id")
        private Long companyId;

        private String company;

        @JsonProperty("generated_at")
        private String generatedAt;

        @JsonProperty("sales_trends")
        private List<SalesTrendItem> salesTrends;

        @JsonProperty("top_products")
        private List<TopProductItem> topProducts;

        @JsonProperty("revenue_analysis")
        private RevenueAnalysis revenueAnalysis;

        @JsonProperty("customer_analysis")
        private CustomerAnalysis customerAnalysis;

        @JsonProperty("product_profitability")
        private List<ProfitabilityItem> productProfitability;

        public Long getCompanyId() { return companyId; }
        public void setCompanyId(Long companyId) { this.companyId = companyId; }

        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }

        public String getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }

        public List<SalesTrendItem> getSalesTrends() { return salesTrends; }
        public void setSalesTrends(List<SalesTrendItem> salesTrends) { this.salesTrends = salesTrends; }

        public List<TopProductItem> getTopProducts() { return topProducts; }
        public void setTopProducts(List<TopProductItem> topProducts) { this.topProducts = topProducts; }

        public RevenueAnalysis getRevenueAnalysis() { return revenueAnalysis; }
        public void setRevenueAnalysis(RevenueAnalysis revenueAnalysis) { this.revenueAnalysis = revenueAnalysis; }

        public CustomerAnalysis getCustomerAnalysis() { return customerAnalysis; }
        public void setCustomerAnalysis(CustomerAnalysis customerAnalysis) { this.customerAnalysis = customerAnalysis; }

        public List<ProfitabilityItem> getProductProfitability() { return productProfitability; }
        public void setProductProfitability(List<ProfitabilityItem> productProfitability) { this.productProfitability = productProfitability; }
    }

    public static class SalesTrendItem {
        private String month;
        private String label;
        private Double revenue;
        private Integer units;
        private Integer transactions;

        public SalesTrendItem(String month, String label, Double revenue, Integer units, Integer transactions) {
            this.month = month;
            this.label = label;
            this.revenue = revenue;
            this.units = units;
            this.transactions = transactions;
        }

        public String getMonth() { return month; }
        public String getLabel() { return label; }
        public Double getRevenue() { return revenue; }
        public Integer getUnits() { return units; }
        public Integer getTransactions() { return transactions; }
    }

    public static class TopProductItem {
        @JsonProperty("product_id")
        private Long productId;
        private String product;
        @JsonProperty("units_sold")
        private Integer unitsSold;
        private Double revenue;
        @JsonProperty("current_stock")
        private Integer currentStock;

        public TopProductItem(Long productId, String product, Integer unitsSold, Double revenue, Integer currentStock) {
            this.productId = productId;
            this.product = product;
            this.unitsSold = unitsSold;
            this.revenue = revenue;
            this.currentStock = currentStock;
        }

        public Long getProductId() { return productId; }
        public String getProduct() { return product; }
        public Integer getUnitsSold() { return unitsSold; }
        public Double getRevenue() { return revenue; }
        public Integer getCurrentStock() { return currentStock; }
    }

    public static class RevenueAnalysis {
        @JsonProperty("total_revenue")
        private Double totalRevenue;
        @JsonProperty("total_units")
        private Integer totalUnits;
        @JsonProperty("total_transactions")
        private Integer totalTransactions;
        @JsonProperty("average_order_value")
        private Double averageOrderValue;
        @JsonProperty("latest_month_revenue")
        private Double latestMonthRevenue;
        @JsonProperty("previous_month_revenue")
        private Double previousMonthRevenue;
        @JsonProperty("revenue_growth_pct")
        private Double revenueGrowthPct;
        @JsonProperty("best_month")
        private String bestMonth;
        @JsonProperty("best_month_revenue")
        private Double bestMonthRevenue;

        public Double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }

        public Integer getTotalUnits() { return totalUnits; }
        public void setTotalUnits(Integer totalUnits) { this.totalUnits = totalUnits; }

        public Integer getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }

        public Double getAverageOrderValue() { return averageOrderValue; }
        public void setAverageOrderValue(Double averageOrderValue) { this.averageOrderValue = averageOrderValue; }

        public Double getLatestMonthRevenue() { return latestMonthRevenue; }
        public void setLatestMonthRevenue(Double latestMonthRevenue) { this.latestMonthRevenue = latestMonthRevenue; }

        public Double getPreviousMonthRevenue() { return previousMonthRevenue; }
        public void setPreviousMonthRevenue(Double previousMonthRevenue) { this.previousMonthRevenue = previousMonthRevenue; }

        public Double getRevenueGrowthPct() { return revenueGrowthPct; }
        public void setRevenueGrowthPct(Double revenueGrowthPct) { this.revenueGrowthPct = revenueGrowthPct; }

        public String getBestMonth() { return bestMonth; }
        public void setBestMonth(String bestMonth) { this.bestMonth = bestMonth; }

        public Double getBestMonthRevenue() { return bestMonthRevenue; }
        public void setBestMonthRevenue(Double bestMonthRevenue) { this.bestMonthRevenue = bestMonthRevenue; }
    }

    public static class CustomerItem {
        @JsonProperty("customer_id")
        private Long customerId;
        private String customer;
        private Integer transactions;
        private Integer units;
        private Double revenue;
        @JsonProperty("last_purchase")
        private String lastPurchase;
        @JsonProperty("days_since_purchase")
        private Long daysSincePurchase;

        public CustomerItem(Long customerId, String customer, Integer transactions, Integer units, Double revenue, String lastPurchase, Long daysSincePurchase) {
            this.customerId = customerId;
            this.customer = customer;
            this.transactions = transactions;
            this.units = units;
            this.revenue = revenue;
            this.lastPurchase = lastPurchase;
            this.daysSincePurchase = daysSincePurchase;
        }

        public Long getCustomerId() { return customerId; }
        public String getCustomer() { return customer; }
        public Integer getTransactions() { return transactions; }
        public Integer getUnits() { return units; }
        public Double getRevenue() { return revenue; }
        public String getLastPurchase() { return lastPurchase; }
        public Long getDaysSincePurchase() { return daysSincePurchase; }
    }

    public static class CustomerAnalysis {
        @JsonProperty("total_customers")
        private Integer totalCustomers;
        @JsonProperty("customers_with_sales")
        private Integer customersWithSales;
        @JsonProperty("repeat_customers")
        private Integer repeatCustomers;
        @JsonProperty("inactive_customers")
        private Integer inactiveCustomers;
        @JsonProperty("top_customers")
        private List<CustomerItem> topCustomers;

        public Integer getTotalCustomers() { return totalCustomers; }
        public void setTotalCustomers(Integer totalCustomers) { this.totalCustomers = totalCustomers; }

        public Integer getCustomersWithSales() { return customersWithSales; }
        public void setCustomersWithSales(Integer customersWithSales) { this.customersWithSales = customersWithSales; }

        public Integer getRepeatCustomers() { return repeatCustomers; }
        public void setRepeatCustomers(Integer repeatCustomers) { this.repeatCustomers = repeatCustomers; }

        public Integer getInactiveCustomers() { return inactiveCustomers; }
        public void setInactiveCustomers(Integer inactiveCustomers) { this.inactiveCustomers = inactiveCustomers; }

        public List<CustomerItem> getTopCustomers() { return topCustomers; }
        public void setTopCustomers(List<CustomerItem> topCustomers) { this.topCustomers = topCustomers; }
    }

    public static class ProfitabilityItem {
        @JsonProperty("product_id")
        private Long productId;
        private String product;
        @JsonProperty("units_sold")
        private Integer unitsSold;
        private Double revenue;
        @JsonProperty("estimated_cost")
        private Double estimatedCost;
        @JsonProperty("gross_profit")
        private Double grossProfit;
        @JsonProperty("margin_pct")
        private Double marginPct;

        public ProfitabilityItem(Long productId, String product, Integer unitsSold, Double revenue, Double estimatedCost, Double grossProfit, Double marginPct) {
            this.productId = productId;
            this.product = product;
            this.unitsSold = unitsSold;
            this.revenue = revenue;
            this.estimatedCost = estimatedCost;
            this.grossProfit = grossProfit;
            this.marginPct = marginPct;
        }

        public Long getProductId() { return productId; }
        public String getProduct() { return product; }
        public Integer getUnitsSold() { return unitsSold; }
        public Double getRevenue() { return revenue; }
        public Double getEstimatedCost() { return estimatedCost; }
        public Double getGrossProfit() { return grossProfit; }
        public Double getMarginPct() { return marginPct; }
    }
}
