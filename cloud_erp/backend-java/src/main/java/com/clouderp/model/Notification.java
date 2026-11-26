package com.clouderp.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "ix_notifications_company_id", columnList = "company_id"),
    @Index(name = "ix_notifications_company_dedup", columnList = "company_id, dedup_key"),
    @Index(name = "ix_notifications_is_read", columnList = "is_read"),
    @Index(name = "ix_notifications_created_at", columnList = "created_at")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    // STOCK_ALERT, INVOICE_OVERDUE, PURCHASE_PENDING, NEW_SALE, PAYMENT_RECEIVED, SYSTEM
    @Column(nullable = false, length = 50)
    private String type;

    // DANGER, WARNING, INFO, SUCCESS
    @Column(nullable = false, length = 20)
    private String severity = "INFO";

    @Column(name = "dedup_key", length = 120)
    private String dedupKey;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "action_url", length = 255)
    private String actionUrl;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    public Notification() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.isRead = false;
        this.severity = "INFO";
    }

    public Notification(Long companyId, String title, String message, String type,
                        String severity, String dedupKey, String actionUrl) {
        this();
        this.companyId = companyId;
        this.title = title;
        this.message = message;
        this.type = type != null ? type : "SYSTEM";
        this.severity = severity != null ? severity : "INFO";
        this.dedupKey = dedupKey;
        this.actionUrl = actionUrl;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDedupKey() {
        return dedupKey;
    }

    public void setDedupKey(String dedupKey) {
        this.dedupKey = dedupKey;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(OffsetDateTime readAt) {
        this.readAt = readAt;
    }
}
