package com.clouderp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SaleDto {

    public static class Create {
        @NotNull(message = "company_id is required")
        @Min(1)
        @JsonProperty("company_id")
        private Long companyId;

        @NotNull(message = "product_id is required")
        @Min(1)
        @JsonProperty("product_id")
        private Long productId;

        @NotNull(message = "quantity is required")
        @Min(1)
        private Integer quantity;

        @JsonProperty("customer_id")
        private Long customerId;

        public Long getCompanyId() {
            return companyId;
        }

        public void setCompanyId(Long companyId) {
            this.companyId = companyId;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }
    }

    public static class Item {
        private Long id;

        @JsonProperty("company_id")
        private Long companyId;

        @JsonProperty("product_id")
        private Long productId;

        private String product;

        @JsonProperty("customer_id")
        private Long customerId;

        private String customer;
        private Integer quantity;

        @JsonProperty("unit_price")
        private Double unitPrice;

        @JsonProperty("total_amount")
        private Double totalAmount;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("invoice_number")
        private String invoiceNumber;

        @JsonProperty("paid_amount")
        private Double paidAmount = 0.0;

        @JsonProperty("balance_amount")
        private Double balanceAmount = 0.0;

        @JsonProperty("payment_status")
        private String paymentStatus = "UNPAID";

        @JsonProperty("due_date")
        private String dueDate;

        public Item() {}

        public Item(Long id, Long companyId, Long productId, String product, Long customerId, String customer,
                    Integer quantity, Double unitPrice, Double totalAmount, String createdAt) {
            this.id = id;
            this.companyId = companyId;
            this.productId = productId;
            this.product = product;
            this.customerId = customerId;
            this.customer = customer;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalAmount = totalAmount;
            this.createdAt = createdAt;
            this.paidAmount = 0.0;
            this.balanceAmount = totalAmount != null ? totalAmount : 0.0;
            this.paymentStatus = "UNPAID";
            this.invoiceNumber = "INV-" + id;
        }

        public Item(Long id, Long companyId, Long productId, String product, Long customerId, String customer,
                    Integer quantity, Double unitPrice, Double totalAmount, String createdAt,
                    String invoiceNumber, Double paidAmount, Double balanceAmount, String paymentStatus, String dueDate) {
            this(id, companyId, productId, product, customerId, customer, quantity, unitPrice, totalAmount, createdAt);
            this.invoiceNumber = invoiceNumber;
            this.paidAmount = paidAmount != null ? paidAmount : 0.0;
            this.balanceAmount = balanceAmount != null ? balanceAmount : 0.0;
            this.paymentStatus = paymentStatus != null ? paymentStatus : "UNPAID";
            this.dueDate = dueDate;
        }

        public String getInvoiceNumber() {
            return invoiceNumber;
        }

        public void setInvoiceNumber(String invoiceNumber) {
            this.invoiceNumber = invoiceNumber;
        }

        public Double getPaidAmount() {
            return paidAmount;
        }

        public void setPaidAmount(Double paidAmount) {
            this.paidAmount = paidAmount;
        }

        public Double getBalanceAmount() {
            return balanceAmount;
        }

        public void setBalanceAmount(Double balanceAmount) {
            this.balanceAmount = balanceAmount;
        }

        public String getPaymentStatus() {
            return paymentStatus;
        }

        public void setPaymentStatus(String paymentStatus) {
            this.paymentStatus = paymentStatus;
        }

        public String getDueDate() {
            return dueDate;
        }

        public void setDueDate(String dueDate) {
            this.dueDate = dueDate;
        }

        public Long getId() {
            return id;
        }

        public Long getCompanyId() {
            return companyId;
        }

        public Long getProductId() {
            return productId;
        }

        public String getProduct() {
            return product;
        }

        public Long getCustomerId() {
            return customerId;
        }

        public String getCustomer() {
            return customer;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public Double getUnitPrice() {
            return unitPrice;
        }

        public Double getTotalAmount() {
            return totalAmount;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }

    public static class CreateResponse {
        private String message;
        private Item sale;

        @JsonProperty("remaining_stock")
        private Integer remainingStock;

        public CreateResponse(String message, Item sale, Integer remainingStock) {
            this.message = message;
            this.sale = sale;
            this.remainingStock = remainingStock;
        }

        public String getMessage() {
            return message;
        }

        public Item getSale() {
            return sale;
        }

        public Integer getRemainingStock() {
            return remainingStock;
        }
    }

    public static class SummaryResponse {
        @JsonProperty("company_id")
        private Long companyId;

        @JsonProperty("total_sales")
        private Double totalSales;

        @JsonProperty("total_units_sold")
        private Integer totalUnitsSold;

        @JsonProperty("total_transactions")
        private Integer totalTransactions;

        public SummaryResponse(Long companyId, Double totalSales, Integer totalUnitsSold, Integer totalTransactions) {
            this.companyId = companyId;
            this.totalSales = totalSales;
            this.totalUnitsSold = totalUnitsSold;
            this.totalTransactions = totalTransactions;
        }

        public Long getCompanyId() {
            return companyId;
        }

        public Double getTotalSales() {
            return totalSales;
        }

        public Integer getTotalUnitsSold() {
            return totalUnitsSold;
        }

        public Integer getTotalTransactions() {
            return totalTransactions;
        }
    }
}
