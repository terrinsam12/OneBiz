package com.clouderp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductDto {

    public static class Create {
        @NotNull(message = "company_id is required")
        @Min(1)
        @JsonProperty("company_id")
        private Long companyId;

        @NotBlank(message = "Product name is required")
        @Size(min = 1, max = 150)
        private String name;

        @NotNull(message = "Price is required")
        @Min(0)
        private Double price;

        @Min(0)
        private Integer quantity = 0;

        @Min(0)
        @JsonProperty("cost_price")
        private Double costPrice = 0.0;

        @Min(0)
        @JsonProperty("reorder_level")
        private Integer reorderLevel = 5;

        public Long getCompanyId() {
            return companyId;
        }

        public void setCompanyId(Long companyId) {
            this.companyId = companyId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Double getCostPrice() {
            return costPrice;
        }

        public void setCostPrice(Double costPrice) {
            this.costPrice = costPrice;
        }

        public Integer getReorderLevel() {
            return reorderLevel;
        }

        public void setReorderLevel(Integer reorderLevel) {
            this.reorderLevel = reorderLevel;
        }
    }

    public static class Response {
        private Long id;

        @JsonProperty("company_id")
        private Long companyId;

        private String name;
        private Double price;
        private Integer quantity;

        @JsonProperty("cost_price")
        private Double costPrice;

        @JsonProperty("reorder_level")
        private Integer reorderLevel;

        public Response(Long id, Long companyId, String name, Double price, Integer quantity, Double costPrice, Integer reorderLevel) {
            this.id = id;
            this.companyId = companyId;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.costPrice = costPrice;
            this.reorderLevel = reorderLevel;
        }

        public Long getId() {
            return id;
        }

        public Long getCompanyId() {
            return companyId;
        }

        public String getName() {
            return name;
        }

        public Double getPrice() {
            return price;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public Double getCostPrice() {
            return costPrice;
        }

        public Integer getReorderLevel() {
            return reorderLevel;
        }
    }
}
