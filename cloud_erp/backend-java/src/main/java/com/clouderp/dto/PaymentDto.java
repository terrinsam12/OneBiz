package com.clouderp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaymentDto {

    public static class RecordRequest {
        @NotNull(message = "sale_id is required")
        @JsonProperty("sale_id")
        private Long saleId;

        @NotNull(message = "Payment amount is required")
        @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
        private Double amount;

        @NotNull(message = "Payment date is required")
        @JsonProperty("payment_date")
        private LocalDate paymentDate;

        @NotBlank(message = "Payment method is required")
        @JsonProperty("payment_method")
        private String paymentMethod = "Cash"; // Cash, UPI, Bank Transfer, Card

        @JsonProperty("reference_number")
        private String referenceNumber;

        private String notes;

        public Long getSaleId() { return saleId; }
        public void setSaleId(Long saleId) { this.saleId = saleId; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }

        public LocalDate getPaymentDate() { return paymentDate; }
        public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class Response {
        private Long id;
        @JsonProperty("company_id")
        private Long companyId;
        @JsonProperty("sale_id")
        private Long saleId;
        @JsonProperty("invoice_number")
        private String invoiceNumber;
        @JsonProperty("customer_id")
        private Long customerId;
        @JsonProperty("customer_name")
        private String customerName;
        @JsonProperty("payment_number")
        private String paymentNumber;
        private Double amount;
        @JsonProperty("payment_date")
        private LocalDate paymentDate;
        @JsonProperty("payment_method")
        private String paymentMethod;
        @JsonProperty("reference_number")
        private String referenceNumber;
        private String notes;
        @JsonProperty("created_at")
        private String createdAt;
        @JsonProperty("invoice_total")
        private Double invoiceTotal;
        @JsonProperty("invoice_balance")
        private Double invoiceBalance;
        @JsonProperty("invoice_payment_status")
        private String invoicePaymentStatus;

        public Response() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getCompanyId() { return companyId; }
        public void setCompanyId(Long companyId) { this.companyId = companyId; }

        public Long getSaleId() { return saleId; }
        public void setSaleId(Long saleId) { this.saleId = saleId; }

        public String getInvoiceNumber() { return invoiceNumber; }
        public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }

        public String getPaymentNumber() { return paymentNumber; }
        public void setPaymentNumber(String paymentNumber) { this.paymentNumber = paymentNumber; }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }

        public LocalDate getPaymentDate() { return paymentDate; }
        public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public Double getInvoiceTotal() { return invoiceTotal; }
        public void setInvoiceTotal(Double invoiceTotal) { this.invoiceTotal = invoiceTotal; }

        public Double getInvoiceBalance() { return invoiceBalance; }
        public void setInvoiceBalance(Double invoiceBalance) { this.invoiceBalance = invoiceBalance; }

        public String getInvoicePaymentStatus() { return invoicePaymentStatus; }
        public void setInvoicePaymentStatus(String invoicePaymentStatus) { this.invoicePaymentStatus = invoicePaymentStatus; }
    }

    public static class ReceivablesSummary {
        @JsonProperty("total_receivables")
        private Double totalReceivables;
        @JsonProperty("total_paid")
        private Double totalPaid;
        @JsonProperty("total_invoiced")
        private Double totalInvoiced;
        @JsonProperty("total_overdue")
        private Double totalOverdue;
        @JsonProperty("overdue_invoices_count")
        private Integer overdueInvoicesCount;
        @JsonProperty("unpaid_invoices_count")
        private Integer unpaidInvoicesCount;
        @JsonProperty("partially_paid_invoices_count")
        private Integer partiallyPaidInvoicesCount;
        @JsonProperty("paid_invoices_count")
        private Integer paidInvoicesCount;
        @JsonProperty("total_invoices_count")
        private Integer totalInvoicesCount;
        @JsonProperty("collection_rate_pct")
        private Double collectionRatePct;

        public ReceivablesSummary() {}

        public Double getTotalReceivables() { return totalReceivables; }
        public void setTotalReceivables(Double totalReceivables) { this.totalReceivables = totalReceivables; }

        public Double getTotalPaid() { return totalPaid; }
        public void setTotalPaid(Double totalPaid) { this.totalPaid = totalPaid; }

        public Double getTotalInvoiced() { return totalInvoiced; }
        public void setTotalInvoiced(Double totalInvoiced) { this.totalInvoiced = totalInvoiced; }

        public Double getTotalOverdue() { return totalOverdue; }
        public void setTotalOverdue(Double totalOverdue) { this.totalOverdue = totalOverdue; }

        public Integer getOverdueInvoicesCount() { return overdueInvoicesCount; }
        public void setOverdueInvoicesCount(Integer overdueInvoicesCount) { this.overdueInvoicesCount = overdueInvoicesCount; }

        public Integer getUnpaidInvoicesCount() { return unpaidInvoicesCount; }
        public void setUnpaidInvoicesCount(Integer unpaidInvoicesCount) { this.unpaidInvoicesCount = unpaidInvoicesCount; }

        public Integer getPartiallyPaidInvoicesCount() { return partiallyPaidInvoicesCount; }
        public void setPartiallyPaidInvoicesCount(Integer partiallyPaidInvoicesCount) { this.partiallyPaidInvoicesCount = partiallyPaidInvoicesCount; }

        public Integer getPaidInvoicesCount() { return paidInvoicesCount; }
        public void setPaidInvoicesCount(Integer paidInvoicesCount) { this.paidInvoicesCount = paidInvoicesCount; }

        public Integer getTotalInvoicesCount() { return totalInvoicesCount; }
        public void setTotalInvoicesCount(Integer totalInvoicesCount) { this.totalInvoicesCount = totalInvoicesCount; }

        public Double getCollectionRatePct() { return collectionRatePct; }
        public void setCollectionRatePct(Double collectionRatePct) { this.collectionRatePct = collectionRatePct; }
    }

    public static class InvoiceReceivable {
        private Long id;
        @JsonProperty("invoice_number")
        private String invoiceNumber;
        @JsonProperty("company_id")
        private Long companyId;
        @JsonProperty("product_id")
        private Long productId;
        @JsonProperty("product_name")
        private String productName;
        @JsonProperty("customer_id")
        private Long customerId;
        @JsonProperty("customer_name")
        private String customerName;
        private Integer quantity;
        @JsonProperty("unit_price")
        private Double unitPrice;
        @JsonProperty("total_amount")
        private Double totalAmount;
        @JsonProperty("paid_amount")
        private Double paidAmount;
        @JsonProperty("balance_amount")
        private Double balanceAmount;
        @JsonProperty("payment_status")
        private String paymentStatus;
        @JsonProperty("due_date")
        private String dueDate;
        @JsonProperty("is_overdue")
        private Boolean isOverdue;
        @JsonProperty("days_overdue")
        private Long daysOverdue;
        @JsonProperty("created_at")
        private String createdAt;
        @JsonProperty("payments_count")
        private Integer paymentsCount;

        public InvoiceReceivable() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getInvoiceNumber() { return invoiceNumber; }
        public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

        public Long getCompanyId() { return companyId; }
        public void setCompanyId(Long companyId) { this.companyId = companyId; }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public Double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

        public Double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

        public Double getPaidAmount() { return paidAmount; }
        public void setPaidAmount(Double paidAmount) { this.paidAmount = paidAmount; }

        public Double getBalanceAmount() { return balanceAmount; }
        public void setBalanceAmount(Double balanceAmount) { this.balanceAmount = balanceAmount; }

        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }

        public Boolean getIsOverdue() { return isOverdue; }
        public void setIsOverdue(Boolean isOverdue) { this.isOverdue = isOverdue; }

        public Long getDaysOverdue() { return daysOverdue; }
        public void setDaysOverdue(Long daysOverdue) { this.daysOverdue = daysOverdue; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public Integer getPaymentsCount() { return paymentsCount; }
        public void setPaymentsCount(Integer paymentsCount) { this.paymentsCount = paymentsCount; }
    }

    public static class CustomerReceivable {
        @JsonProperty("customer_id")
        private Long customerId;
        @JsonProperty("customer_name")
        private String customerName;
        private String phone;
        private String email;
        @JsonProperty("total_invoiced")
        private Double totalInvoiced;
        @JsonProperty("total_paid")
        private Double totalPaid;
        @JsonProperty("outstanding_balance")
        private Double outstandingBalance;
        @JsonProperty("overdue_balance")
        private Double overdueBalance;
        @JsonProperty("invoices_count")
        private Integer invoicesCount;
        @JsonProperty("unpaid_invoices_count")
        private Integer unpaidInvoicesCount;
        @JsonProperty("last_payment_date")
        private String lastPaymentDate;

        public CustomerReceivable() {}

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public Double getTotalInvoiced() { return totalInvoiced; }
        public void setTotalInvoiced(Double totalInvoiced) { this.totalInvoiced = totalInvoiced; }

        public Double getTotalPaid() { return totalPaid; }
        public void setTotalPaid(Double totalPaid) { this.totalPaid = totalPaid; }

        public Double getOutstandingBalance() { return outstandingBalance; }
        public void setOutstandingBalance(Double outstandingBalance) { this.outstandingBalance = outstandingBalance; }

        public Double getOverdueBalance() { return overdueBalance; }
        public void setOverdueBalance(Double overdueBalance) { this.overdueBalance = overdueBalance; }

        public Integer getInvoicesCount() { return invoicesCount; }
        public void setInvoicesCount(Integer invoicesCount) { this.invoicesCount = invoicesCount; }

        public Integer getUnpaidInvoicesCount() { return unpaidInvoicesCount; }
        public void setUnpaidInvoicesCount(Integer unpaidInvoicesCount) { this.unpaidInvoicesCount = unpaidInvoicesCount; }

        public String getLastPaymentDate() { return lastPaymentDate; }
        public void setLastPaymentDate(String lastPaymentDate) { this.lastPaymentDate = lastPaymentDate; }
    }
}
