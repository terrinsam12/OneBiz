package com.clouderp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDto {

    public static class RegisterRequest {
        @NotBlank(message = "Company name is required")
        @Size(min = 1, max = 150)
        @JsonProperty("company_name")
        private String companyName;

        @NotBlank(message = "Owner name is required")
        @Size(min = 1, max = 150)
        @JsonProperty("owner_name")
        private String ownerName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        private String email;

        private String phone = "";

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @JsonProperty("business_type")
        private String businessType = "";

        public String getCompanyName() {
            return companyName;
        }

        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public void setOwnerName(String ownerName) {
            this.ownerName = ownerName;
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

        public String getBusinessType() {
            return businessType;
        }

        public void setBusinessType(String businessType) {
            this.businessType = businessType;
        }
    }

    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email address")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class UserSummary {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String role;

        @JsonProperty("company_id")
        private Long companyId;

        @JsonProperty("is_active")
        private Boolean isActive;

        public UserSummary() {}

        public UserSummary(Long id, String name, String email, String role, Long companyId) {
            this(id, name, email, "", role, companyId, true);
        }

        public UserSummary(Long id, String name, String email, String phone, String role, Long companyId, Boolean isActive) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.role = role;
            this.companyId = companyId;
            this.isActive = isActive;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getRole() { return role; }
        public Long getCompanyId() { return companyId; }
        public Boolean getIsActive() { return isActive; }
    }

    public static class CompanySummary {
        private Long id;
        private String name;

        @JsonProperty("business_type")
        private String businessType;

        private String industry;

        @JsonProperty("owner_name")
        private String ownerName;

        private String email;
        private String phone;

        public CompanySummary(Long id, String name, String businessType, String industry, String ownerName, String email, String phone) {
            this.id = id;
            this.name = name;
            this.businessType = businessType;
            this.industry = industry;
            this.ownerName = ownerName;
            this.email = email;
            this.phone = phone;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public String getBusinessType() { return businessType; }
        public String getIndustry() { return industry; }
        public String getOwnerName() { return ownerName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
    }

    public static class AuthResponse {
        private String token;

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("token_type")
        private String tokenType = "Bearer";

        private UserSummary user;
        private CompanySummary company;

        public AuthResponse(String token, UserSummary user, CompanySummary company) {
            this.token = token;
            this.accessToken = token;
            this.user = user;
            this.company = company;
        }

        public String getToken() { return token; }
        public String getAccessToken() { return accessToken; }
        public String getTokenType() { return tokenType; }
        public UserSummary getUser() { return user; }
        public CompanySummary getCompany() { return company; }
    }
}
