package com.clouderp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class FinanceDto {

    public static class SummaryResponse {
        @JsonProperty("company_id")
        private Long companyId;

        @JsonProperty("company_name")
        private String companyName;

        @JsonProperty("currency")
        private String currency = "INR";

        @JsonProperty("start_date")
        private String startDate;

        @JsonProperty("end_date")
        private String endDate;

        // Sales & Revenue
        @JsonProperty("total_revenue")
        private Double totalRevenue;

        @JsonProperty("total_sales_count")
        private Integer totalSalesCount;

        @JsonProperty("total_units_sold")
        private Integer totalUnitsSold;

        // COGS & Gross Profit
        @JsonProperty("cogs")
        private Double cogs;

        @JsonProperty("gross_profit")
        private Double grossProfit;

        @JsonProperty("gross_profit_margin")
        private Double grossProfitMargin;

        // Operating Expenses
        @JsonProperty("total_operating_expenses")
        private Double totalOperatingExpenses;

        @JsonProperty("expense_count")
        private Integer expenseCount;

        // Net Operating Profit
        @JsonProperty("net_operating_profit")
        private Double netOperatingProfit;

        @JsonProperty("net_operating_profit_margin")
        private Double netOperatingProfitMargin;

        // Procurement / Purchases
        @JsonProperty("procurement_spending")
        private Double procurementSpending;

        @JsonProperty("procurement_count")
        private Integer procurementCount;

        @JsonProperty("pending_purchases_amount")
        private Double pendingPurchasesAmount;

        @JsonProperty("pending_purchases_count")
        private Integer pendingPurchasesCount;

        @JsonProperty("cancelled_purchases_count")
        private Integer cancelledPurchasesCount;

        // Customer Receivables Integration
        @JsonProperty("total_receivables")
        private Double totalReceivables = 0.0;

        @JsonProperty("total_overdue_receivables")
        private Double totalOverdueReceivables = 0.0;

        // Best & Lowest Performing Months
        @JsonProperty("best_month_revenue")
        private MonthPerformance bestMonthRevenue;

        @JsonProperty("lowest_month_revenue")
        private MonthPerformance lowestMonthRevenue;

        @JsonProperty("best_month_profit")
        private MonthPerformance bestMonthProfit;

        @JsonProperty("lowest_month_profit")
        private MonthPerformance lowestMonthProfit;

        // Visual Breakdown & Trends
        @JsonProperty("monthly_trends")
        private List<MonthlyFinancialItem> monthlyTrends = new ArrayList<>();

        @JsonProperty("expense_breakdown")
        private List<ExpenseCategoryItem> expenseBreakdown = new ArrayList<>();

        @JsonProperty("product_profitability")
        private List<ProductProfitabilityItem> productProfitability = new ArrayList<>();

        public SummaryResponse() {}

        public Long getCompanyId() { return companyId; }
        public void setCompanyId(Long companyId) { this.companyId = companyId; }

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }

        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }

        public Double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }

        public Integer getTotalSalesCount() { return totalSalesCount; }
        public void setTotalSalesCount(Integer totalSalesCount) { this.totalSalesCount = totalSalesCount; }

        public Integer getTotalUnitsSold() { return totalUnitsSold; }
        public void setTotalUnitsSold(Integer totalUnitsSold) { this.totalUnitsSold = totalUnitsSold; }

        public Double getCogs() { return cogs; }
        public void setCogs(Double cogs) { this.cogs = cogs; }

        public Double getGrossProfit() { return grossProfit; }
        public void setGrossProfit(Double grossProfit) { this.grossProfit = grossProfit; }

        public Double getGrossProfitMargin() { return grossProfitMargin; }
        public void setGrossProfitMargin(Double grossProfitMargin) { this.grossProfitMargin = grossProfitMargin; }

        public Double getTotalOperatingExpenses() { return totalOperatingExpenses; }
        public void setTotalOperatingExpenses(Double totalOperatingExpenses) { this.totalOperatingExpenses = totalOperatingExpenses; }

        public Integer getExpenseCount() { return expenseCount; }
        public void setExpenseCount(Integer expenseCount) { this.expenseCount = expenseCount; }

        public Double getNetOperatingProfit() { return netOperatingProfit; }
        public void setNetOperatingProfit(Double netOperatingProfit) { this.netOperatingProfit = netOperatingProfit; }

        public Double getNetOperatingProfitMargin() { return netOperatingProfitMargin; }
        public void setNetOperatingProfitMargin(Double netOperatingProfitMargin) { this.netOperatingProfitMargin = netOperatingProfitMargin; }

        public Double getProcurementSpending() { return procurementSpending; }
        public void setProcurementSpending(Double procurementSpending) { this.procurementSpending = procurementSpending; }

        public Integer getProcurementCount() { return procurementCount; }
        public void setProcurementCount(Integer procurementCount) { this.procurementCount = procurementCount; }

        public Double getPendingPurchasesAmount() { return pendingPurchasesAmount; }
        public void setPendingPurchasesAmount(Double pendingPurchasesAmount) { this.pendingPurchasesAmount = pendingPurchasesAmount; }

        public Integer getPendingPurchasesCount() { return pendingPurchasesCount; }
        public void setPendingPurchasesCount(Integer pendingPurchasesCount) { this.pendingPurchasesCount = pendingPurchasesCount; }

        public Integer getCancelledPurchasesCount() { return cancelledPurchasesCount; }
        public void setCancelledPurchasesCount(Integer cancelledPurchasesCount) { this.cancelledPurchasesCount = cancelledPurchasesCount; }

        public Double getTotalReceivables() { return totalReceivables; }
        public void setTotalReceivables(Double totalReceivables) { this.totalReceivables = totalReceivables; }

        public Double getTotalOverdueReceivables() { return totalOverdueReceivables; }
        public void setTotalOverdueReceivables(Double totalOverdueReceivables) { this.totalOverdueReceivables = totalOverdueReceivables; }

        public MonthPerformance getBestMonthRevenue() { return bestMonthRevenue; }
        public void setBestMonthRevenue(MonthPerformance bestMonthRevenue) { this.bestMonthRevenue = bestMonthRevenue; }

        public MonthPerformance getLowestMonthRevenue() { return lowestMonthRevenue; }
        public void setLowestMonthRevenue(MonthPerformance lowestMonthRevenue) { this.lowestMonthRevenue = lowestMonthRevenue; }

        public MonthPerformance getBestMonthProfit() { return bestMonthProfit; }
        public void setBestMonthProfit(MonthPerformance bestMonthProfit) { this.bestMonthProfit = bestMonthProfit; }

        public MonthPerformance getLowestMonthProfit() { return lowestMonthProfit; }
        public void setLowestMonthProfit(MonthPerformance lowestMonthProfit) { this.lowestMonthProfit = lowestMonthProfit; }

        public List<MonthlyFinancialItem> getMonthlyTrends() { return monthlyTrends; }
        public void setMonthlyTrends(List<MonthlyFinancialItem> monthlyTrends) { this.monthlyTrends = monthlyTrends; }

        public List<ExpenseCategoryItem> getExpenseBreakdown() { return expenseBreakdown; }
        public void setExpenseBreakdown(List<ExpenseCategoryItem> expenseBreakdown) { this.expenseBreakdown = expenseBreakdown; }

        public List<ProductProfitabilityItem> getProductProfitability() { return productProfitability; }
        public void setProductProfitability(List<ProductProfitabilityItem> productProfitability) { this.productProfitability = productProfitability; }
    }

    public static class MonthPerformance {
        private String month;
        private String label;
        private Double amount;

        public MonthPerformance() {}

        public MonthPerformance(String month, String label, Double amount) {
            this.month = month;
            this.label = label;
            this.amount = amount;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }

    public static class MonthlyFinancialItem {
        private String month;
        private String label;
        private Double revenue;
        private Double cogs;

        @JsonProperty("gross_profit")
        private Double grossProfit;

        @JsonProperty("operating_expenses")
        private Double operatingExpenses;

        @JsonProperty("net_profit")
        private Double netProfit;

        @JsonProperty("procurement_spending")
        private Double procurementSpending;

        @JsonProperty("gross_margin_pct")
        private Double grossMarginPct;

        @JsonProperty("net_margin_pct")
        private Double netMarginPct;

        public MonthlyFinancialItem() {}

        public MonthlyFinancialItem(String month, String label, Double revenue, Double cogs, Double grossProfit,
                                    Double operatingExpenses, Double netProfit, Double procurementSpending,
                                    Double grossMarginPct, Double netMarginPct) {
            this.month = month;
            this.label = label;
            this.revenue = revenue;
            this.cogs = cogs;
            this.grossProfit = grossProfit;
            this.operatingExpenses = operatingExpenses;
            this.netProfit = netProfit;
            this.procurementSpending = procurementSpending;
            this.grossMarginPct = grossMarginPct;
            this.netMarginPct = netMarginPct;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public Double getRevenue() { return revenue; }
        public void setRevenue(Double revenue) { this.revenue = revenue; }

        public Double getCogs() { return cogs; }
        public void setCogs(Double cogs) { this.cogs = cogs; }

        public Double getGrossProfit() { return grossProfit; }
        public void setGrossProfit(Double grossProfit) { this.grossProfit = grossProfit; }

        public Double getOperatingExpenses() { return operatingExpenses; }
        public void setOperatingExpenses(Double operatingExpenses) { this.operatingExpenses = operatingExpenses; }

        public Double getNetProfit() { return netProfit; }
        public void setNetProfit(Double netProfit) { this.netProfit = netProfit; }

        public Double getProcurementSpending() { return procurementSpending; }
        public void setProcurementSpending(Double procurementSpending) { this.procurementSpending = procurementSpending; }

        public Double getGrossMarginPct() { return grossMarginPct; }
        public void setGrossMarginPct(Double grossMarginPct) { this.grossMarginPct = grossMarginPct; }

        public Double getNetMarginPct() { return netMarginPct; }
        public void setNetMarginPct(Double netMarginPct) { this.netMarginPct = netMarginPct; }
    }

    public static class ExpenseCategoryItem {
        private String category;
        private Double amount;
        private Double percentage;
        private Integer count;

        public ExpenseCategoryItem() {}

        public ExpenseCategoryItem(String category, Double amount, Double percentage, Integer count) {
            this.category = category;
            this.amount = amount;
            this.percentage = percentage;
            this.count = count;
        }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }

        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }

        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

    public static class ProductProfitabilityItem {
        @JsonProperty("product_id")
        private Long productId;

        @JsonProperty("product_name")
        private String productName;

        @JsonProperty("units_sold")
        private Integer unitsSold;

        private Double revenue;
        private Double cogs;

        @JsonProperty("gross_profit")
        private Double grossProfit;

        @JsonProperty("margin_pct")
        private Double marginPct;

        public ProductProfitabilityItem() {}

        public ProductProfitabilityItem(Long productId, String productName, Integer unitsSold,
                                      Double revenue, Double cogs, Double grossProfit, Double marginPct) {
            this.productId = productId;
            this.productName = productName;
            this.unitsSold = unitsSold;
            this.revenue = revenue;
            this.cogs = cogs;
            this.grossProfit = grossProfit;
            this.marginPct = marginPct;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public Integer getUnitsSold() { return unitsSold; }
        public void setUnitsSold(Integer unitsSold) { this.unitsSold = unitsSold; }

        public Double getRevenue() { return revenue; }
        public void setRevenue(Double revenue) { this.revenue = revenue; }

        public Double getCogs() { return cogs; }
        public void setCogs(Double cogs) { this.cogs = cogs; }

        public Double getGrossProfit() { return grossProfit; }
        public void setGrossProfit(Double grossProfit) { this.grossProfit = grossProfit; }

        public Double getMarginPct() { return marginPct; }
        public void setMarginPct(Double marginPct) { this.marginPct = marginPct; }
    }
}
