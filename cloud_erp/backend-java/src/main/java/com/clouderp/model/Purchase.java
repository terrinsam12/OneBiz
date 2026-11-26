package com.clouderp.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "purchases", indexes = {
    @Index(name = "ix_purchases_company_id", columnList = "company_id"),
    @Index(name = "ix_purchases_supplier_id", columnList = "supplier_id"),
    @Index(name = "ix_purchases_status", columnList = "status")
})
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "purchase_number", nullable = false)
    private String purchaseNumber;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, RECEIVED, CANCELLED

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount = 0.0;

    @Column(length = 2000)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "received_at")
    private OffsetDateTime receivedAt;

    public Purchase() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.status = "PENDING";
        this.totalAmount = 0.0;
    }

    public Purchase(Long companyId, Long supplierId, String purchaseNumber, String status, Double totalAmount, String notes) {
        this();
        this.companyId = companyId;
        this.supplierId = supplierId;
        this.purchaseNumber = purchaseNumber;
        this.status = status != null ? status : "PENDING";
        this.totalAmount = totalAmount != null ? totalAmount : 0.0;
        this.notes = notes;
        if ("RECEIVED".equalsIgnoreCase(this.status)) {
            this.receivedAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
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

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public String getPurchaseNumber() {
        return purchaseNumber;
    }

    public void setPurchaseNumber(String purchaseNumber) {
        this.purchaseNumber = purchaseNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
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

    public OffsetDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(OffsetDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
}
