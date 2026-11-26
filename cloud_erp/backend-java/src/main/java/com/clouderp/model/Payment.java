package com.clouderp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "customer_payments", indexes = {
    @Index(name = "ix_payments_company_id", columnList = "company_id"),
    @Index(name = "ix_payments_sale_id", columnList = "sale_id"),
    @Index(name = "ix_payments_customer_id", columnList = "customer_id"),
    @Index(name = "ix_payments_payment_date", columnList = "payment_date")
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "sale_id", nullable = false)
    private Long saleId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "payment_number", nullable = false)
    private String paymentNumber;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // Cash, UPI, Bank Transfer, Card

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(length = 2000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Payment() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.paymentDate = LocalDate.now(ZoneOffset.UTC);
    }

    public Payment(Long companyId, Long saleId, Long customerId, String paymentNumber,
                   Double amount, LocalDate paymentDate, String paymentMethod,
                   String referenceNumber, String notes) {
        this();
        this.companyId = companyId;
        this.saleId = saleId;
        this.customerId = customerId;
        this.paymentNumber = paymentNumber;
        this.amount = amount;
        this.paymentDate = paymentDate != null ? paymentDate : LocalDate.now(ZoneOffset.UTC);
        this.paymentMethod = paymentMethod;
        this.referenceNumber = referenceNumber;
        this.notes = notes;
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

    public Long getSaleId() {
        return saleId;
    }

    public void setSaleId(Long saleId) {
        this.saleId = saleId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
