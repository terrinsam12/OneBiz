package com.clouderp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public class NotificationDto {

    public static class Response {
        private Long id;

        @JsonProperty("company_id")
        private Long companyId;

        private String title;
        private String message;
        private String type;
        private String severity;

        @JsonProperty("dedup_key")
        private String dedupKey;

        @JsonProperty("is_read")
        private Boolean isRead;

        @JsonProperty("action_url")
        private String actionUrl;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("read_at")
        private String readAt;

        @JsonProperty("time_ago")
        private String timeAgo;

        public Response() {}

        public Response(Long id, Long companyId, String title, String message, String type,
                        String severity, String dedupKey, Boolean isRead, String actionUrl,
                        String createdAt, String readAt, String timeAgo) {
            this.id = id;
            this.companyId = companyId;
            this.title = title;
            this.message = message;
            this.type = type;
            this.severity = severity;
            this.dedupKey = dedupKey;
            this.isRead = isRead;
            this.actionUrl = actionUrl;
            this.createdAt = createdAt;
            this.readAt = readAt;
            this.timeAgo = timeAgo;
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

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getReadAt() {
            return readAt;
        }

        public void setReadAt(String readAt) {
            this.readAt = readAt;
        }

        public String getTimeAgo() {
            return timeAgo;
        }

        public void setTimeAgo(String timeAgo) {
            this.timeAgo = timeAgo;
        }
    }

    public static class UnreadCountResponse {
        @JsonProperty("unread_count")
        private long unreadCount;

        public UnreadCountResponse() {}

        public UnreadCountResponse(long unreadCount) {
            this.unreadCount = unreadCount;
        }

        public long getUnreadCount() {
            return unreadCount;
        }

        public void setUnreadCount(long unreadCount) {
            this.unreadCount = unreadCount;
        }
    }

    public static class ScanResponse {
        @JsonProperty("generated_alerts")
        private int generatedAlerts;

        @JsonProperty("total_unread")
        private long totalUnread;

        private String message;

        public ScanResponse() {}

        public ScanResponse(int generatedAlerts, long totalUnread, String message) {
            this.generatedAlerts = generatedAlerts;
            this.totalUnread = totalUnread;
            this.message = message;
        }

        public int getGeneratedAlerts() {
            return generatedAlerts;
        }

        public void setGeneratedAlerts(int generatedAlerts) {
            this.generatedAlerts = generatedAlerts;
        }

        public long getTotalUnread() {
            return totalUnread;
        }

        public void setTotalUnread(long totalUnread) {
            this.totalUnread = totalUnread;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
