package com.clouderp.dto;

import com.clouderp.model.Supplier;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public class SupplierDto {

    public static class Create {
        @NotBlank(message = "Supplier name is required")
        @Size(max = 150, message = "Supplier name must not exceed 150 characters")
        private String name;

        @JsonProperty("contact_person")
        private String contactPerson = "";

        private String phone = "";

        private String email = "";

        private String address = "";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContactPerson() {
            return contactPerson;
        }

        public void setContactPerson(String contactPerson) {
            this.contactPerson = contactPerson;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    public static class Update {
        @NotBlank(message = "Supplier name is required")
        @Size(max = 150, message = "Supplier name must not exceed 150 characters")
        private String name;

        @JsonProperty("contact_person")
        private String contactPerson = "";

        private String phone = "";

        private String email = "";

        private String address = "";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContactPerson() {
            return contactPerson;
        }

        public void setContactPerson(String contactPerson) {
            this.contactPerson = contactPerson;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    public static class Response {
        private Long id;
        private String name;

        @JsonProperty("contact_person")
        private String contactPerson;

        private String phone;
        private String email;
        private String address;

        @JsonProperty("company_id")
        private Long companyId;

        @JsonProperty("created_at")
        private OffsetDateTime createdAt;

        @JsonProperty("purchase_count")
        private Long purchaseCount = 0L;

        @JsonProperty("total_spend")
        private Double totalSpend = 0.0;

        public Response() {
        }

        public Response(Long id, String name, String contactPerson, String phone, String email, String address, Long companyId, OffsetDateTime createdAt) {
            this.id = id;
            this.name = name;
            this.contactPerson = contactPerson;
            this.phone = phone;
            this.email = email;
            this.address = address;
            this.companyId = companyId;
            this.createdAt = createdAt;
        }

        public static Response fromSupplier(Supplier supplier) {
            return new Response(
                    supplier.getId(),
                    supplier.getName(),
                    supplier.getContactPerson(),
                    supplier.getPhone(),
                    supplier.getEmail(),
                    supplier.getAddress(),
                    supplier.getCompanyId(),
                    supplier.getCreatedAt()
            );
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getContactPerson() {
            return contactPerson;
        }

        public String getPhone() {
            return phone;
        }

        public String getEmail() {
            return email;
        }

        public String getAddress() {
            return address;
        }

        public Long getCompanyId() {
            return companyId;
        }

        public OffsetDateTime getCreatedAt() {
            return createdAt;
        }

        public Long getPurchaseCount() {
            return purchaseCount;
        }

        public void setPurchaseCount(Long purchaseCount) {
            this.purchaseCount = purchaseCount;
        }

        public Double getTotalSpend() {
            return totalSpend;
        }

        public void setTotalSpend(Double totalSpend) {
            this.totalSpend = totalSpend;
        }
    }
}
