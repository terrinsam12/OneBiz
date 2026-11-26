package com.clouderp.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "ix_users_company_id", columnList = "company_id"),
    @Index(name = "ix_users_email", columnList = "email", unique = true)
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column
    private String phone;

    @Column(nullable = false)
    private String role = "COMPANY_ADMIN";

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public User() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.isActive = true;
    }

    public User(Long companyId, String email, String password, String name, String phone, String role) {
        this();
        this.companyId = companyId;
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = role != null ? role : "COMPANY_ADMIN";
        this.isActive = true;
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

    public Boolean getIsActive() {
        return isActive != null ? isActive : true;
    }

    public boolean isActive() {
        return isActive != null ? isActive : true;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive != null ? isActive : true;
    }

    public void setActive(Boolean active) {
        this.isActive = active != null ? active : true;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
