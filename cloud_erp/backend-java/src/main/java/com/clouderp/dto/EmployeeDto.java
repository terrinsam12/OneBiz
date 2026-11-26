package com.clouderp.dto;

import com.clouderp.model.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public class EmployeeDto {

    public static class CreateRequest {
        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Valid email address is required")
        private String email;

        private String phone = "";

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @NotBlank(message = "Role is required")
        @Pattern(regexp = "COMPANY_ADMIN|MANAGER|STAFF", message = "Role must be COMPANY_ADMIN, MANAGER, or STAFF")
        private String role = "STAFF";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public static class UpdateRequest {
        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        private String name;

        private String phone = "";

        @Pattern(regexp = "COMPANY_ADMIN|MANAGER|STAFF", message = "Role must be COMPANY_ADMIN, MANAGER, or STAFF")
        private String role;

        @Size(min = 6, message = "Password must be at least 6 characters if updating")
        private String password;

        @JsonProperty("is_active")
        private Boolean isActive;

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

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Boolean getIsActive() {
            return isActive;
        }

        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }
    }

    public static class StatusUpdateRequest {
        @JsonProperty("is_active")
        private Boolean isActive;

        public Boolean getIsActive() {
            return isActive;
        }

        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }
    }

    public static class Response {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String role;

        @JsonProperty("is_active")
        private Boolean isActive;

        @JsonProperty("company_id")
        private Long companyId;

        @JsonProperty("created_at")
        private OffsetDateTime createdAt;

        public Response() {
        }

        public Response(Long id, String name, String email, String phone, String role, Boolean isActive, Long companyId, OffsetDateTime createdAt) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.role = role;
            this.isActive = isActive;
            this.companyId = companyId;
            this.createdAt = createdAt;
        }

        public static Response fromUser(User user) {
            return new Response(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getRole(),
                    user.getIsActive(),
                    user.getCompanyId(),
                    user.getCreatedAt()
            );
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public String getRole() {
            return role;
        }

        public Boolean getIsActive() {
            return isActive;
        }

        public Long getCompanyId() {
            return companyId;
        }

        public OffsetDateTime getCreatedAt() {
            return createdAt;
        }
    }
}
