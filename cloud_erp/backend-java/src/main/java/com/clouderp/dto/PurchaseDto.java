package com.clouderp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDto {

    public static class ItemRequest {
        @NotNull(message = "Product ID is required")
        @JsonProperty("product_id")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        @Min(value = 0, message = "Unit price must be non-negative")
        @JsonProperty("unit_price")
        private Double unitPrice;

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

        public Double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(Double unitPrice) {
            this.unitPrice = unitPrice;
        }
    }

    public static class Create {
        @NotNull(message = "Supplier ID is required")
        @JsonProperty("supplier_id")
        private Long supplierId;

        @Pattern(regexp = "PENDING|RECEIVED", message = "Initial status must be PENDING or RECEIVED")
        private String status = "PENDING";

        private String notes = "";

        @NotEmpty(message = "Purchase must contain at least one product item")
        @Valid
        private List<ItemRequest> items = new ArrayList<>();

        public Long getSupplierId() {
            return supplierId;
        }

        public void setSupplierId(Long supplierId) {
            this.supplierId = supplierId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public List<ItemRequest> getItems() {
            return items;
        }

        public void setItems(List<ItemRequest> items) {
            this.items = items;
        }
    }

    public static class StatusUpdate {
        @NotNull(message = "Status is required")
        @Pattern(regexp = "PENDING|RECEIVED|CANCELLED", message = "Status must be PENDING, RECEIVED, or CANCELLED")
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class ItemResponse {
        private Long id;

        @JsonProperty("purchase_id")
        private Long purchaseId;

        @JsonProperty("product_id")
        private Long productId;

        @JsonProperty("product_name")
        private String productName;

        private Integer quantity;

        @JsonProperty("unit_price")
        private Double unitPrice;

        @JsonProperty("total_price")
        private Double totalPrice;

        public ItemResponse() {
        }

        public ItemResponse(Long id, Long purchaseId, Long productId, String productName, Integer quantity, Double unitPrice, Double totalPrice) {
            this.id = id;
            this.purchaseId = purchaseId;
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
        }

        public Long getId() {
            return id;
        }

        public Long getPurchaseId() {
            return purchaseId;
        }

        public Long getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public Double getUnitPrice() {
            return unitPrice;
        }

        public Double getTotalPrice() {
            return totalPrice;
        }
    }

    public static class Response {
        private Long id;

        @JsonProperty("company_id")
        private Long companyId;

        @JsonProperty("supplier_id")
        private Long supplierId;

        @JsonProperty("supplier_name")
        private String supplierName;

        @JsonProperty("purchase_number")
        private String purchaseNumber;

        private String status;

        @JsonProperty("total_amount")
        private Double totalAmount;

        private String notes;

        @JsonProperty("created_at")
        private OffsetDateTime createdAt;

        @JsonProperty("received_at")
        private OffsetDateTime receivedAt;

        private List<ItemResponse> items = new ArrayList<>();

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

        public String getSupplierName() {
            return supplierName;
        }

        public void setSupplierName(String supplierName) {
            this.supplierName = supplierName;
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

        public List<ItemResponse> getItems() {
            return items;
        }

        public void setItems(List<ItemResponse> items) {
            this.items = items;
        }
    }

    public static class SummaryResponse {
        @JsonProperty("total_orders")
        private Long totalOrders;

        @JsonProperty("pending_orders")
        private Long pendingOrders;

        @JsonProperty("received_orders")
        private Long receivedOrders;

        @JsonProperty("cancelled_orders")
        private Long cancelledOrders;

        @JsonProperty("total_spend")
        private Double totalSpend;

        public SummaryResponse(Long totalOrders, Long pendingOrders, Long receivedOrders, Long cancelledOrders, Double totalSpend) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.receivedOrders = receivedOrders;
            this.cancelledOrders = cancelledOrders;
            this.totalSpend = totalSpend;
        }

        public Long getTotalOrders() {
            return totalOrders;
        }

        public Long getPendingOrders() {
            return pendingOrders;
        }

        public Long getReceivedOrders() {
            return receivedOrders;
        }

        public Long getCancelledOrders() {
            return cancelledOrders;
        }

        public Double getTotalSpend() {
            return totalSpend;
        }
    }
}
