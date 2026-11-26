package com.clouderp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "expenses", indexes = {
    @Index(name = "ix_expenses_company_id", columnList = "company_id"),
    @Index(name = "ix_expenses_category", columnList = "category"),
    @Index(name = "ix_expenses_expense_date", columnList = "expense_date")
})
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod = "Cash";

    @Column(length = 2000)
    private String description;

    @Column(name = "receipt_url", length = 2000)
    private String receiptUrl;

    @Column(name = "receipt_name")
    private String receiptName;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Expense() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.expenseDate = LocalDate.now();
        this.paymentMethod = "Cash";
    }

    public Expense(Long companyId, String title, String category, Double amount, LocalDate expenseDate, String paymentMethod, String description, String receiptUrl, String receiptName) {
        this();
        this.companyId = companyId;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.expenseDate = expenseDate != null ? expenseDate : LocalDate.now();
        this.paymentMethod = paymentMethod != null ? paymentMethod : "Cash";
        this.description = description;
        this.receiptUrl = receiptUrl;
        this.receiptName = receiptName;
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
