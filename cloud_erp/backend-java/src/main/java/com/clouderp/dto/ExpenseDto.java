package com.clouderp.dto;

import com.clouderp.model.Expense;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDto {

    public static class Create {
        @NotBlank(message = "Expense title is required")
        @Size(max = 150, message = "Title must not exceed 150 characters")
        private String title;

        @NotBlank(message = "Category is required")
        private String category;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        private Double amount;

        @NotNull(message = "Expense date is required")
        @JsonProperty("expense_date")
        private LocalDate expenseDate;

        @NotBlank(message = "Payment method is required")
        @JsonProperty("payment_method")
        private String paymentMethod = "Cash";

        private String description = "";

        @JsonProperty("receipt_url")
        private String receiptUrl;

        @JsonProperty("receipt_name")
        private String receiptName;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }

        public LocalDate getExpenseDate() {
            return expenseDate;
        }

        public void setExpenseDate(LocalDate expenseDate) {
            this.expenseDate = expenseDate;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getReceiptUrl() {
            return receiptUrl;
        }

        public void setReceiptUrl(String receiptUrl) {
            this.receiptUrl = receiptUrl;
        }

        public String getReceiptName() {
            return receiptName;
        }

        public void setReceiptName(String receiptName) {
            this.receiptName = receiptName;
        }
    }

    public static class Update {
        @NotBlank(message = "Expense title is required")
        @Size(max = 150, message = "Title must not exceed 150 characters")
        private String title;

        @NotBlank(message = "Category is required")
        private String category;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        private Double amount;

        @NotNull(message = "Expense date is required")
        @JsonProperty("expense_date")
        private LocalDate expenseDate;

        @NotBlank(message = "Payment method is required")
        @JsonProperty("payment_method")
        private String paymentMethod = "Cash";

        private String description = "";

        @JsonProperty("receipt_url")
        private String receiptUrl;

        @JsonProperty("receipt_name")
        private String receiptName;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }

        public LocalDate getExpenseDate() {
            return expenseDate;
        }

        public void setExpenseDate(LocalDate expenseDate) {
            this.expenseDate = expenseDate;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getReceiptUrl() {
            return receiptUrl;
        }

        public void setReceiptUrl(String receiptUrl) {
            this.receiptUrl = receiptUrl;
        }

        public String getReceiptName() {
            return receiptName;
        }

        public void setReceiptName(String receiptName) {
            this.receiptName = receiptName;
        }
    }

    public static class Response {
        private Long id;

        @JsonProperty("company_id")
        private Long companyId;

        private String title;
        private String category;
        private Double amount;

        @JsonProperty("expense_date")
        private LocalDate expenseDate;

        @JsonProperty("payment_method")
        private String paymentMethod;

        private String description;

        @JsonProperty("receipt_url")
        private String receiptUrl;

        @JsonProperty("receipt_name")
        private String receiptName;

        @JsonProperty("created_at")
        private OffsetDateTime createdAt;

        public Response() {
        }

        public Response(Long id, Long companyId, String title, String category, Double amount, LocalDate expenseDate, String paymentMethod, String description, String receiptUrl, String receiptName, OffsetDateTime createdAt) {
            this.id = id;
            this.companyId = companyId;
            this.title = title;
            this.category = category;
            this.amount = amount;
            this.expenseDate = expenseDate;
            this.paymentMethod = paymentMethod;
            this.description = description;
            this.receiptUrl = receiptUrl;
            this.receiptName = receiptName;
            this.createdAt = createdAt;
        }

        public static Response fromEntity(Expense exp) {
            return new Response(
                    exp.getId(),
                    exp.getCompanyId(),
                    exp.getTitle(),
                    exp.getCategory(),
                    exp.getAmount(),
                    exp.getExpenseDate(),
                    exp.getPaymentMethod(),
                    exp.getDescription(),
                    exp.getReceiptUrl(),
                    exp.getReceiptName(),
                    exp.getCreatedAt()
            );
        }

        public Long getId() { return id; }
        public Long getCompanyId() { return companyId; }
        public String getTitle() { return title; }
        public String getCategory() { return category; }
        public Double getAmount() { return amount; }
        public LocalDate getExpenseDate() { return expenseDate; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getDescription() { return description; }
        public String getReceiptUrl() { return receiptUrl; }
        public String getReceiptName() { return receiptName; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
    }

    public static class CategorySummary {
        private String category;
        private Double amount;
        private Long count;
        private Double percentage;

        public CategorySummary() {}

        public CategorySummary(String category, Double amount, Long count, Double percentage) {
            this.category = category;
            this.amount = amount;
            this.count = count;
            this.percentage = percentage;
        }

        public String getCategory() { return category; }
        public Double getAmount() { return amount; }
        public Long getCount() { return count; }
        public Double getPercentage() { return percentage; }
    }

    public static class MonthlyTrend {
        private String month;
        private String label;
        private Double amount;
        private Long count;

        public MonthlyTrend() {}

        public MonthlyTrend(String month, String label, Double amount, Long count) {
            this.month = month;
            this.label = label;
            this.amount = amount;
            this.count = count;
        }

        public String getMonth() { return month; }
        public String getLabel() { return label; }
        public Double getAmount() { return amount; }
        public Long getCount() { return count; }
    }

    public static class SummaryResponse {
        @JsonProperty("total_expenses")
        private Double totalExpenses;

        @JsonProperty("this_month_expenses")
        private Double thisMonthExpenses;

        @JsonProperty("last_month_expenses")
        private Double lastMonthExpenses;

        @JsonProperty("expense_count")
        private Long expenseCount;

        @JsonProperty("top_category")
        private String topCategory;

        @JsonProperty("top_category_amount")
        private Double topCategoryAmount;

        @JsonProperty("category_breakdown")
        private List<CategorySummary> categoryBreakdown = new ArrayList<>();

        @JsonProperty("monthly_trends")
        private List<MonthlyTrend> monthlyTrends = new ArrayList<>();

        public SummaryResponse() {}

        public SummaryResponse(Double totalExpenses, Double thisMonthExpenses, Double lastMonthExpenses, Long expenseCount, String topCategory, Double topCategoryAmount, List<CategorySummary> categoryBreakdown, List<MonthlyTrend> monthlyTrends) {
            this.totalExpenses = totalExpenses;
            this.thisMonthExpenses = thisMonthExpenses;
            this.lastMonthExpenses = lastMonthExpenses;
            this.expenseCount = expenseCount;
            this.topCategory = topCategory;
            this.topCategoryAmount = topCategoryAmount;
            this.categoryBreakdown = categoryBreakdown;
            this.monthlyTrends = monthlyTrends;
        }

        public Double getTotalExpenses() { return totalExpenses; }
        public Double getThisMonthExpenses() { return thisMonthExpenses; }
        public Double getLastMonthExpenses() { return lastMonthExpenses; }
        public Long getExpenseCount() { return expenseCount; }
        public String getTopCategory() { return topCategory; }
        public Double getTopCategoryAmount() { return topCategoryAmount; }
        public List<CategorySummary> getCategoryBreakdown() { return categoryBreakdown; }
        public List<MonthlyTrend> getMonthlyTrends() { return monthlyTrends; }
    }

    public static class UploadResponse {
        @JsonProperty("file_url")
        private String fileUrl;

        @JsonProperty("file_name")
        private String fileName;

        public UploadResponse(String fileUrl, String fileName) {
            this.fileUrl = fileUrl;
            this.fileName = fileName;
        }

        public String getFileUrl() { return fileUrl; }
        public String getFileName() { return fileName; }
    }
}
