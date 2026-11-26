package com.clouderp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class InventoryDto {

    public static class Request {
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
    }

    public static class StockOperationResponse {
        private String message;

        @JsonProperty("company_id")
        private Long companyId;

        @JsonProperty("product_id")
        private Long productId;

        private String product;

        @JsonProperty("stock_added")
        private Integer stockAdded;

        @JsonProperty("stock_removed")
        private Integer stockRemoved;

        @JsonProperty("current_stock")
        private Integer currentStock;

        @JsonProperty("reorder_level")
        private Integer reorderLevel;

        public static StockOperationResponse forStockIn(Long companyId, Long productId, String product, int added, int current, int reorder) {
            StockOperationResponse res = new StockOperationResponse();
            res.message = "Stock added successfully";
            res.companyId = companyId;
            res.productId = productId;
            res.product = product;
            res.stockAdded = added;
            res.currentStock = current;
            res.reorderLevel = reorder;
            return res;
        }

        public static StockOperationResponse forStockOut(Long companyId, Long productId, String product, int removed, int current, int reorder) {
            StockOperationResponse res = new StockOperationResponse();
            res.message = "Stock removed successfully";
            res.companyId = companyId;
            res.productId = productId;
            res.product = product;
            res.stockRemoved = removed;
            res.currentStock = current;
            res.reorderLevel = reorder;
            return res;
        }

        public String getMessage() {
            return message;
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

        public Integer getStockAdded() {
            return stockAdded;
        }

        public Integer getStockRemoved() {
            return stockRemoved;
        }

        public Integer getCurrentStock() {
            return currentStock;
        }

        public Integer getReorderLevel() {
            return reorderLevel;
        }
    }

    public static class Item {
        @JsonProperty("product_id")
        private Long productId;

        private String product;
        private Double price;
        private Integer quantity;

        @JsonProperty("reorder_level")
        private Integer reorderLevel;

        @JsonProperty("stock_status")
        private String stockStatus;

        public Item(Long productId, String product, Double price, Integer quantity, Integer reorderLevel, String stockStatus) {
            this.productId = productId;
            this.product = product;
            this.price = price;
            this.quantity = quantity;
            this.reorderLevel = reorderLevel;
            this.stockStatus = stockStatus;
        }

        public Long getProductId() {
            return productId;
        }

        public String getProduct() {
            return product;
        }

        public Double getPrice() {
            return price;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public Integer getReorderLevel() {
            return reorderLevel;
        }

        public String getStockStatus() {
            return stockStatus;
        }
    }

    public static class Response {
        @JsonProperty("company_id")
        private Long companyId;

        private String company;
        private List<Item> inventory;

        public Response(Long companyId, String company, List<Item> inventory) {
            this.companyId = companyId;
            this.company = company;
            this.inventory = inventory;
        }

        public Long getCompanyId() {
            return companyId;
        }

        public String getCompany() {
            return company;
        }

        public List<Item> getInventory() {
            return inventory;
        }
    }
}
