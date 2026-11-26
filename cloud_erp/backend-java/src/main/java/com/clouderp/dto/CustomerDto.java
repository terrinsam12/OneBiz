package com.clouderp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CustomerDto {

    public static class Create {
        @NotNull(message = "company_id is required")
        @Min(1)
        @JsonProperty("company_id")
        private Long companyId;

        @NotBlank(message = "Customer name is required")
        @Size(min = 1, max = 150)
        private String name;

        private String phone = "";
        private String email = "";
        private String address = "";

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

        @JsonProperty("company_id")
        private Long companyId;

        private String name;
        private String phone;
        private String email;
        private String address;

        public Response(Long id, Long companyId, String name, String phone, String email, String address) {
            this.id = id;
            this.companyId = companyId;
            this.name = name;
            this.phone = phone;
            this.email = email;
            this.address = address;
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

        public String getPhone() {
            return phone;
        }

        public String getEmail() {
            return email;
        }

        public String getAddress() {
            return address;
        }
    }

    public static class ListResponse {
        @JsonProperty("company_id")
        private Long companyId;

        private String company;
        private List<Response> customers;

        public ListResponse(Long companyId, String company, List<Response> customers) {
            this.companyId = companyId;
            this.company = company;
            this.customers = customers;
        }

        public Long getCompanyId() {
            return companyId;
        }

        public String getCompany() {
            return company;
        }

        public List<Response> getCustomers() {
            return customers;
        }
    }
}
