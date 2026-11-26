package com.clouderp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "sales", indexes = {
    @Index(name = "ix_sales_company_id", columnList = "company_id"),
    @Index(name = "ix_sales_product_id", columnList = "product_id"),
    @Index(name = "ix_sales_customer_id", columnList = "customer_id"),
    @Index(name = "ix_sales_payment_status", columnList = "payment_status")
})
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "paid_amount", nullable = false)
    private Double paidAmount = 0.0;

    @Column(name = "balance_amount", nullable = false)
    private Double balanceAmount = 0.0;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus = "UNPAID";

    @Column(name = "due_date")
    private LocalDate dueDate;

    public Sale() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.paidAmount = 0.0;
        this.balanceAmount = 0.0;
        this.paymentStatus = "UNPAID";
        this.dueDate = LocalDate.now(ZoneOffset.UTC).plusDays(15);
    }

    public Sale(Long companyId, Long productId, Long customerId, Integer quantity, Double unitPrice, Double totalAmount) {
        this();
        this.companyId = companyId;
        this.productId = productId;
        this.customerId = customerId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.balanceAmount = totalAmount != null ? totalAmount : 0.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Double getPaidAmount() {
        return paidAmount != null ? paidAmount : 0.0;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Double getBalanceAmount() {
        return balanceAmount != null ? balanceAmount : 0.0;
    }

    public void setBalanceAmount(Double balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus != null ? paymentStatus : "UNPAID";
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
