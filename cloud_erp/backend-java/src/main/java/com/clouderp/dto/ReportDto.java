package com.clouderp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class ReportDto {

    // Common Date Filter metadata
    public static class FilterInfo {
        @JsonProperty("start_date")
        private String startDate;

        @JsonProperty("end_date")
        private String endDate;

        private String preset;

        @JsonProperty("period_label")
        private String periodLabel;

        public FilterInfo() {}

        public FilterInfo(String startDate, String endDate, String preset, String periodLabel) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.preset = preset;
            this.periodLabel = periodLabel;
        }

        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getPreset() { return preset; }
        public void setPreset(String preset) { this.preset = preset; }
        public String getPeriodLabel() { return periodLabel; }
        public void setPeriodLabel(String periodLabel) { this.periodLabel = periodLabel; }
    }

    // 1. SALES REPORT
    public static class SalesReport {
        @JsonProperty("company_name")
        private String companyName;
        private FilterInfo filter;

        @JsonProperty("total_revenue")
        private Double totalRevenue = 0.0;

        @JsonProperty("total_transactions")
        private Integer totalTransactions = 0;

        @JsonProperty("total_units_sold")
        private Integer totalUnitsSold = 0;

        @JsonProperty("average_order_value")
        private Double averageOrderValue = 0.0;

        @JsonProperty("top_products")
        private List<TopProductItem> topProducts;

        private List<SaleTransactionItem> transactions;

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public FilterInfo getFilter() { return filter; }
        public void setFilter(FilterInfo filter) { this.filter = filter; }
        public Double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
        public Integer getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }
        public Integer getTotalUnitsSold() { return totalUnitsSold; }
        public void setTotalUnitsSold(Integer totalUnitsSold) { this.totalUnitsSold = totalUnitsSold; }
        public Double getAverageOrderValue() { return averageOrderValue; }
        public void setAverageOrderValue(Double averageOrderValue) { this.averageOrderValue = averageOrderValue; }
        public List<TopProductItem> getTopProducts() { return topProducts; }
        public void setTopProducts(List<TopProductItem> topProducts) { this.topProducts = topProducts; }
        public List<SaleTransactionItem> getTransactions() { return transactions; }
        public void setTransactions(List<SaleTransactionItem> transactions) { this.transactions = transactions; }
    }

    public static class TopProductItem {
        @JsonProperty("product_id")
        private Long productId;
        @JsonProperty("product_name")
        private String productName;
        @JsonProperty("units_sold")
        private Integer unitsSold;
        @JsonProperty("total_revenue")
        private Double totalRevenue;
        @JsonProperty("avg_selling_price")
        private Double avgSellingPrice;

        public TopProductItem() {}
        public TopProductItem(Long productId, String productName, Integer unitsSold, Double totalRevenue, Double avgSellingPrice) {
            this.productId = productId;
            this.productName = productName;
            this.unitsSold = unitsSold;
            this.totalRevenue = totalRevenue;
            this.avgSellingPrice = avgSellingPrice;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getUnitsSold() { return unitsSold; }
        public void setUnitsSold(Integer unitsSold) { this.unitsSold = unitsSold; }
        public Double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
        public Double getAvgSellingPrice() { return avgSellingPrice; }
        public void setAvgSellingPrice(Double avgSellingPrice) { this.avgSellingPrice = avgSellingPrice; }
    }

    public static class SaleTransactionItem {
        private Long id;
        @JsonProperty("invoice_number")
        private String invoiceNumber;
        @JsonProperty("sale_date")
        private String saleDate;
        @JsonProperty("customer_name")
        private String customerName;
        @JsonProperty("product_name")
        private String productName;
        private Integer quantity;
        @JsonProperty("unit_price")
        private Double unitPrice;
        @JsonProperty("total_amount")
        private Double totalAmount;
        @JsonProperty("payment_status")
        private String paymentStatus;
        @JsonProperty("paid_amount")
        private Double paidAmount;
        @JsonProperty("balance_amount")
        private Double balanceAmount;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getInvoiceNumber() { return invoiceNumber; }
        public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
        public String getSaleDate() { return saleDate; }
        public void setSaleDate(String saleDate) { this.saleDate = saleDate; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
        public Double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        public Double getPaidAmount() { return paidAmount; }
        public void setPaidAmount(Double paidAmount) { this.paidAmount = paidAmount; }
        public Double getBalanceAmount() { return balanceAmount; }
        public void setBalanceAmount(Double balanceAmount) { this.balanceAmount = balanceAmount; }
    }

    // 2. PURCHASE REPORT
    public static class PurchaseReport {
        @JsonProperty("company_name")
        private String companyName;
        private FilterInfo filter;

        @JsonProperty("total_procurement_spending")
        private Double totalProcurementSpending = 0.0;

        @JsonProperty("total_purchases_count")
        private Integer totalPurchasesCount = 0;

        @JsonProperty("received_purchases_count")
        private Integer receivedPurchasesCount = 0;

        @JsonProperty("pending_purchases_count")
        private Integer pendingPurchasesCount = 0;

        @JsonProperty("pending_amount")
        private Double pendingAmount = 0.0;

        @JsonProperty("cancelled_purchases_count")
        private Integer cancelledPurchasesCount = 0;

        @JsonProperty("supplier_breakdown")
        private List<SupplierPurchaseItem> supplierBreakdown;

        private List<PurchaseReportItem> purchases;

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public FilterInfo getFilter() { return filter; }
        public void setFilter(FilterInfo filter) { this.filter = filter; }
        public Double getTotalProcurementSpending() { return totalProcurementSpending; }
        public void setTotalProcurementSpending(Double totalProcurementSpending) { this.totalProcurementSpending = totalProcurementSpending; }
        public Integer getTotalPurchasesCount() { return totalPurchasesCount; }
        public void setTotalPurchasesCount(Integer totalPurchasesCount) { this.totalPurchasesCount = totalPurchasesCount; }
        public Integer getReceivedPurchasesCount() { return receivedPurchasesCount; }
        public void setReceivedPurchasesCount(Integer receivedPurchasesCount) { this.receivedPurchasesCount = receivedPurchasesCount; }
        public Integer getPendingPurchasesCount() { return pendingPurchasesCount; }
        public void setPendingPurchasesCount(Integer pendingPurchasesCount) { this.pendingPurchasesCount = pendingPurchasesCount; }
        public Double getPendingAmount() { return pendingAmount; }
        public void setPendingAmount(Double pendingAmount) { this.pendingAmount = pendingAmount; }
        public Integer getCancelledPurchasesCount() { return cancelledPurchasesCount; }
        public void setCancelledPurchasesCount(Integer cancelledPurchasesCount) { this.cancelledPurchasesCount = cancelledPurchasesCount; }
        public List<SupplierPurchaseItem> getSupplierBreakdown() { return supplierBreakdown; }
        public void setSupplierBreakdown(List<SupplierPurchaseItem> supplierBreakdown) { this.supplierBreakdown = supplierBreakdown; }
        public List<PurchaseReportItem> getPurchases() { return purchases; }
        public void setPurchases(List<PurchaseReportItem> purchases) { this.purchases = purchases; }
    }

    public static class SupplierPurchaseItem {
        @JsonProperty("supplier_id")
        private Long supplierId;
        @JsonProperty("supplier_name")
        private String supplierName;
        @JsonProperty("contact_person")
        private String contactPerson;
        private String phone;
        @JsonProperty("total_orders")
        private Integer totalOrders = 0;
        @JsonProperty("received_orders")
        private Integer receivedOrders = 0;
        @JsonProperty("total_spending")
        private Double totalSpending = 0.0;
        @JsonProperty("last_order_date")
        private String lastOrderDate;

        public Long getSupplierId() { return supplierId; }
        public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
        public String getSupplierName() { return supplierName; }
        public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
        public String getContactPerson() { return contactPerson; }
        public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public Integer getTotalOrders() { return totalOrders; }
        public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
        public Integer getReceivedOrders() { return receivedOrders; }
        public void setReceivedOrders(Integer receivedOrders) { this.receivedOrders = receivedOrders; }
        public Double getTotalSpending() { return totalSpending; }
        public void setTotalSpending(Double totalSpending) { this.totalSpending = totalSpending; }
        public String getLastOrderDate() { return lastOrderDate; }
        public void setLastOrderDate(String lastOrderDate) { this.lastOrderDate = lastOrderDate; }
    }

    public static class PurchaseReportItem {
        private Long id;
        @JsonProperty("purchase_number")
        private String purchaseNumber;
        @JsonProperty("supplier_name")
        private String supplierName;
        private String status;
        @JsonProperty("total_amount")
        private Double totalAmount;
        @JsonProperty("created_at")
        private String createdAt;
        @JsonProperty("received_at")
        private String receivedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getPurchaseNumber() { return purchaseNumber; }
        public void setPurchaseNumber(String purchaseNumber) { this.purchaseNumber = purchaseNumber; }
        public String getSupplierName() { return supplierName; }
        public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getReceivedAt() { return receivedAt; }
        public void setReceivedAt(String receivedAt) { this.receivedAt = receivedAt; }
    }

    // 3. INVENTORY REPORT
    public static class InventoryReport {
        @JsonProperty("company_name")
        private String companyName;
        @JsonProperty("generated_date")
        private String generatedDate;

        @JsonProperty("total_products")
        private Integer totalProducts = 0;

        @JsonProperty("total_stock_units")
        private Integer totalStockUnits = 0;

        @JsonProperty("total_valuation_cost")
        private Double totalValuationCost = 0.0;

        @JsonProperty("total_valuation_retail")
        private Double totalValuationRetail = 0.0;

        @JsonProperty("potential_gross_margin")
        private Double potentialGrossMargin = 0.0;

        @JsonProperty("low_stock_count")
        private Integer lowStockCount = 0;

        @JsonProperty("out_of_stock_count")
        private Integer outOfStockCount = 0;

        @JsonProperty("low_stock_alerts")
        private List<ProductInventoryItem> lowStockAlerts;

        private List<ProductInventoryItem> products;

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public String getGeneratedDate() { return generatedDate; }
        public void setGeneratedDate(String generatedDate) { this.generatedDate = generatedDate; }
        public Integer getTotalProducts() { return totalProducts; }
        public void setTotalProducts(Integer totalProducts) { this.totalProducts = totalProducts; }
        public Integer getTotalStockUnits() { return totalStockUnits; }
        public void setTotalStockUnits(Integer totalStockUnits) { this.totalStockUnits = totalStockUnits; }
        public Double getTotalValuationCost() { return totalValuationCost; }
        public void setTotalValuationCost(Double totalValuationCost) { this.totalValuationCost = totalValuationCost; }
        public Double getTotalValuationRetail() { return totalValuationRetail; }
        public void setTotalValuationRetail(Double totalValuationRetail) { this.totalValuationRetail = totalValuationRetail; }
        public Double getPotentialGrossMargin() { return potentialGrossMargin; }
        public void setPotentialGrossMargin(Double potentialGrossMargin) { this.potentialGrossMargin = potentialGrossMargin; }
        public Integer getLowStockCount() { return lowStockCount; }
        public void setLowStockCount(Integer lowStockCount) { this.lowStockCount = lowStockCount; }
        public Integer getOutOfStockCount() { return outOfStockCount; }
        public void setOutOfStockCount(Integer outOfStockCount) { this.outOfStockCount = outOfStockCount; }
        public List<ProductInventoryItem> getLowStockAlerts() { return lowStockAlerts; }
        public void setLowStockAlerts(List<ProductInventoryItem> lowStockAlerts) { this.lowStockAlerts = lowStockAlerts; }
        public List<ProductInventoryItem> getProducts() { return products; }
        public void setProducts(List<ProductInventoryItem> products) { this.products = products; }
    }

    public static class ProductInventoryItem {
        private Long id;
        private String name;
        @JsonProperty("cost_price")
        private Double costPrice;
        @JsonProperty("selling_price")
        private Double sellingPrice;
        private Integer quantity;
        @JsonProperty("reorder_level")
        private Integer reorderLevel;
        @JsonProperty("valuation_cost")
        private Double valuationCost;
        @JsonProperty("valuation_retail")
        private Double valuationRetail;
        @JsonProperty("stock_status")
        private String stockStatus; // IN_STOCK, LOW_STOCK, OUT_OF_STOCK

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getCostPrice() { return costPrice; }
        public void setCostPrice(Double costPrice) { this.costPrice = costPrice; }
        public Double getSellingPrice() { return sellingPrice; }
        public void setSellingPrice(Double sellingPrice) { this.sellingPrice = sellingPrice; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Integer getReorderLevel() { return reorderLevel; }
        public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }
        public Double getValuationCost() { return valuationCost; }
        public void setValuationCost(Double valuationCost) { this.valuationCost = valuationCost; }
        public Double getValuationRetail() { return valuationRetail; }
        public void setValuationRetail(Double valuationRetail) { this.valuationRetail = valuationRetail; }
        public String getStockStatus() { return stockStatus; }
        public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }
    }

    // 4. EXPENSE REPORT
    public static class ExpenseReport {
        @JsonProperty("company_name")
        private String companyName;
        private FilterInfo filter;

        @JsonProperty("total_expenses")
        private Double totalExpenses = 0.0;

        @JsonProperty("total_transactions")
        private Integer totalTransactions = 0;

        @JsonProperty("average_expense")
        private Double averageExpense = 0.0;

        @JsonProperty("category_breakdown")
        private List<CategoryExpenseItem> categoryBreakdown;

        @JsonProperty("monthly_breakdown")
        private List<MonthlyExpenseItem> monthlyBreakdown;

        private List<ExpenseReportItem> expenses;

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public FilterInfo getFilter() { return filter; }
        public void setFilter(FilterInfo filter) { this.filter = filter; }
        public Double getTotalExpenses() { return totalExpenses; }
        public void setTotalExpenses(Double totalExpenses) { this.totalExpenses = totalExpenses; }
        public Integer getTotalTransactions() { return totalTransactions; }
        public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }
        public Double getAverageExpense() { return averageExpense; }
        public void setAverageExpense(Double averageExpense) { this.averageExpense = averageExpense; }
        public List<CategoryExpenseItem> getCategoryBreakdown() { return categoryBreakdown; }
        public void setCategoryBreakdown(List<CategoryExpenseItem> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }
        public List<MonthlyExpenseItem> getMonthlyBreakdown() { return monthlyBreakdown; }
        public void setMonthlyBreakdown(List<MonthlyExpenseItem> monthlyBreakdown) { this.monthlyBreakdown = monthlyBreakdown; }
        public List<ExpenseReportItem> getExpenses() { return expenses; }
        public void setExpenses(List<ExpenseReportItem> expenses) { this.expenses = expenses; }
    }

    public static class CategoryExpenseItem {
        private String category;
        private Double amount;
        private Integer count;
        @JsonProperty("percentage_of_total")
        private Double percentageOfTotal;

        public CategoryExpenseItem() {}
        public CategoryExpenseItem(String category, Double amount, Integer count, Double percentageOfTotal) {
            this.category = category;
            this.amount = amount;
            this.count = count;
            this.percentageOfTotal = percentageOfTotal;
        }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
        public Double getPercentageOfTotal() { return percentageOfTotal; }
        public void setPercentageOfTotal(Double percentageOfTotal) { this.percentageOfTotal = percentageOfTotal; }
    }

    public static class MonthlyExpenseItem {
        private String month;
        @JsonProperty("month_label")
        private String monthLabel;
        private Double amount;
        private Integer count;

        public MonthlyExpenseItem() {}
        public MonthlyExpenseItem(String month, String monthLabel, Double amount, Integer count) {
            this.month = month;
            this.monthLabel = monthLabel;
            this.amount = amount;
            this.count = count;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public String getMonthLabel() { return monthLabel; }
        public void setMonthLabel(String monthLabel) { this.monthLabel = monthLabel; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }

    public static class ExpenseReportItem {
        private Long id;
        private String name;
        private String category;
        private Double amount;
        @JsonProperty("expense_date")
        private String expenseDate;
        @JsonProperty("payment_method")
        private String paymentMethod;
        private String description;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public String getExpenseDate() { return expenseDate; }
        public void setExpenseDate(String expenseDate) { this.expenseDate = expenseDate; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    // 5. PROFIT & LOSS REPORT
    public static class ProfitLossReport {
        @JsonProperty("company_name")
        private String companyName;
        private FilterInfo filter;

        @JsonProperty("gross_sales_revenue")
        private Double grossSalesRevenue = 0.0;

        @JsonProperty("cost_of_goods_sold")
        private Double costOfGoodsSold = 0.0;

        @JsonProperty("gross_profit")
        private Double grossProfit = 0.0;

        @JsonProperty("gross_profit_margin_pct")
        private Double grossProfitMarginPct = 0.0;

        @JsonProperty("total_operating_expenses")
        private Double totalOperatingExpenses = 0.0;

        @JsonProperty("operating_expenses_by_category")
        private List<CategoryExpenseItem> operatingExpensesByCategory;

        @JsonProperty("net_operating_profit")
        private Double netOperatingProfit = 0.0;

        @JsonProperty("net_operating_profit_margin_pct")
        private Double netOperatingProfitMarginPct = 0.0;

        @JsonProperty("monthly_comparison")
        private List<MonthlyProfitLossItem> monthlyComparison;

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public FilterInfo getFilter() { return filter; }
        public void setFilter(FilterInfo filter) { this.filter = filter; }
        public Double getGrossSalesRevenue() { return grossSalesRevenue; }
        public void setGrossSalesRevenue(Double grossSalesRevenue) { this.grossSalesRevenue = grossSalesRevenue; }
        public Double getCostOfGoodsSold() { return costOfGoodsSold; }
        public void setCostOfGoodsSold(Double costOfGoodsSold) { this.costOfGoodsSold = costOfGoodsSold; }
        public Double getGrossProfit() { return grossProfit; }
        public void setGrossProfit(Double grossProfit) { this.grossProfit = grossProfit; }
        public Double getGrossProfitMarginPct() { return grossProfitMarginPct; }
        public void setGrossProfitMarginPct(Double grossProfitMarginPct) { this.grossProfitMarginPct = grossProfitMarginPct; }
        public Double getTotalOperatingExpenses() { return totalOperatingExpenses; }
        public void setTotalOperatingExpenses(Double totalOperatingExpenses) { this.totalOperatingExpenses = totalOperatingExpenses; }
        public List<CategoryExpenseItem> getOperatingExpensesByCategory() { return operatingExpensesByCategory; }
        public void setOperatingExpensesByCategory(List<CategoryExpenseItem> operatingExpensesByCategory) { this.operatingExpensesByCategory = operatingExpensesByCategory; }
        public Double getNetOperatingProfit() { return netOperatingProfit; }
        public void setNetOperatingProfit(Double netOperatingProfit) { this.netOperatingProfit = netOperatingProfit; }
        public Double getNetOperatingProfitMarginPct() { return netOperatingProfitMarginPct; }
        public void setNetOperatingProfitMarginPct(Double netOperatingProfitMarginPct) { this.netOperatingProfitMarginPct = netOperatingProfitMarginPct; }
        public List<MonthlyProfitLossItem> getMonthlyComparison() { return monthlyComparison; }
        public void setMonthlyComparison(List<MonthlyProfitLossItem> monthlyComparison) { this.monthlyComparison = monthlyComparison; }
    }

    public static class MonthlyProfitLossItem {
        private String month;
        @JsonProperty("month_label")
        private String monthLabel;
        private Double revenue = 0.0;
        private Double cogs = 0.0;
        @JsonProperty("gross_profit")
        private Double grossProfit = 0.0;
        @JsonProperty("operating_expenses")
        private Double operatingExpenses = 0.0;
        @JsonProperty("net_operating_profit")
        private Double netOperatingProfit = 0.0;
        @JsonProperty("net_margin_pct")
        private Double netMarginPct = 0.0;

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public String getMonthLabel() { return monthLabel; }
        public void setMonthLabel(String monthLabel) { this.monthLabel = monthLabel; }
        public Double getRevenue() { return revenue; }
        public void setRevenue(Double revenue) { this.revenue = revenue; }
        public Double getCogs() { return cogs; }
        public void setCogs(Double cogs) { this.cogs = cogs; }
        public Double getGrossProfit() { return grossProfit; }
        public void setGrossProfit(Double grossProfit) { this.grossProfit = grossProfit; }
        public Double getOperatingExpenses() { return operatingExpenses; }
        public void setOperatingExpenses(Double operatingExpenses) { this.operatingExpenses = operatingExpenses; }
        public Double getNetOperatingProfit() { return netOperatingProfit; }
        public void setNetOperatingProfit(Double netOperatingProfit) { this.netOperatingProfit = netOperatingProfit; }
        public Double getNetMarginPct() { return netMarginPct; }
        public void setNetMarginPct(Double netMarginPct) { this.netMarginPct = netMarginPct; }
    }

    // 6. RECEIVABLES REPORT
    public static class ReceivablesReport {
        @JsonProperty("company_name")
        private String companyName;
        private FilterInfo filter;

        @JsonProperty("total_invoiced")
        private Double totalInvoiced = 0.0;

        @JsonProperty("total_paid")
        private Double totalPaid = 0.0;

        @JsonProperty("total_receivables")
        private Double totalReceivables = 0.0;

        @JsonProperty("total_overdue")
        private Double totalOverdue = 0.0;

        @JsonProperty("collection_rate_pct")
        private Double collectionRatePct = 0.0;

        @JsonProperty("paid_count")
        private Integer paidCount = 0;

        @JsonProperty("partially_paid_count")
        private Integer partiallyPaidCount = 0;

        @JsonProperty("unpaid_count")
        private Integer unpaidCount = 0;

        @JsonProperty("overdue_count")
        private Integer overdueCount = 0;

        @JsonProperty("customer_breakdown")
        private List<CustomerReceivableItem> customerBreakdown;

        private List<InvoiceReceivableItem> invoices;

        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public FilterInfo getFilter() { return filter; }
        public void setFilter(FilterInfo filter) { this.filter = filter; }
        public Double getTotalInvoiced() { return totalInvoiced; }
        public void setTotalInvoiced(Double totalInvoiced) { this.totalInvoiced = totalInvoiced; }
        public Double getTotalPaid() { return totalPaid; }
        public void setTotalPaid(Double totalPaid) { this.totalPaid = totalPaid; }
        public Double getTotalReceivables() { return totalReceivables; }
        public void setTotalReceivables(Double totalReceivables) { this.totalReceivables = totalReceivables; }
        public Double getTotalOverdue() { return totalOverdue; }
        public void setTotalOverdue(Double totalOverdue) { this.totalOverdue = totalOverdue; }
        public Double getCollectionRatePct() { return collectionRatePct; }
        public void setCollectionRatePct(Double collectionRatePct) { this.collectionRatePct = collectionRatePct; }
        public Integer getPaidCount() { return paidCount; }
        public void setPaidCount(Integer paidCount) { this.paidCount = paidCount; }
        public Integer getPartiallyPaidCount() { return partiallyPaidCount; }
        public void setPartiallyPaidCount(Integer partiallyPaidCount) { this.partiallyPaidCount = partiallyPaidCount; }
        public Integer getUnpaidCount() { return unpaidCount; }
        public void setUnpaidCount(Integer unpaidCount) { this.unpaidCount = unpaidCount; }
        public Integer getOverdueCount() { return overdueCount; }
        public void setOverdueCount(Integer overdueCount) { this.overdueCount = overdueCount; }
        public List<CustomerReceivableItem> getCustomerBreakdown() { return customerBreakdown; }
        public void setCustomerBreakdown(List<CustomerReceivableItem> customerBreakdown) { this.customerBreakdown = customerBreakdown; }
        public List<InvoiceReceivableItem> getInvoices() { return invoices; }
        public void setInvoices(List<InvoiceReceivableItem> invoices) { this.invoices = invoices; }
    }

    public static class CustomerReceivableItem {
        @JsonProperty("customer_id")
        private Long customerId;
        @JsonProperty("customer_name")
        private String customerName;
        private String phone;
        @JsonProperty("total_invoiced")
        private Double totalInvoiced = 0.0;
        @JsonProperty("total_paid")
        private Double totalPaid = 0.0;
        @JsonProperty("outstanding_balance")
        private Double outstandingBalance = 0.0;
        @JsonProperty("overdue_balance")
        private Double overdueBalance = 0.0;
        private String status;

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public Double getTotalInvoiced() { return totalInvoiced; }
        public void setTotalInvoiced(Double totalInvoiced) { this.totalInvoiced = totalInvoiced; }
        public Double getTotalPaid() { return totalPaid; }
        public void setTotalPaid(Double totalPaid) { this.totalPaid = totalPaid; }
        public Double getOutstandingBalance() { return outstandingBalance; }
        public void setOutstandingBalance(Double outstandingBalance) { this.outstandingBalance = outstandingBalance; }
        public Double getOverdueBalance() { return overdueBalance; }
        public void setOverdueBalance(Double overdueBalance) { this.overdueBalance = overdueBalance; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class InvoiceReceivableItem {
        private Long id;
        @JsonProperty("invoice_number")
        private String invoiceNumber;
        @JsonProperty("sale_date")
        private String saleDate;
        @JsonProperty("due_date")
        private String dueDate;
        @JsonProperty("customer_name")
        private String customerName;
        @JsonProperty("total_amount")
        private Double totalAmount;
        @JsonProperty("paid_amount")
        private Double paidAmount;
        @JsonProperty("balance_amount")
        private Double balanceAmount;
        @JsonProperty("payment_status")
        private String paymentStatus;
        @JsonProperty("is_overdue")
        private Boolean isOverdue;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getInvoiceNumber() { return invoiceNumber; }
        public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
        public String getSaleDate() { return saleDate; }
        public void setSaleDate(String saleDate) { this.saleDate = saleDate; }
        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public Double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
        public Double getPaidAmount() { return paidAmount; }
        public void setPaidAmount(Double paidAmount) { this.paidAmount = paidAmount; }
        public Double getBalanceAmount() { return balanceAmount; }
        public void setBalanceAmount(Double balanceAmount) { this.balanceAmount = balanceAmount; }
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        public Boolean getIsOverdue() { return isOverdue; }
        public void setIsOverdue(Boolean overdue) { isOverdue = overdue; }
    }
}
