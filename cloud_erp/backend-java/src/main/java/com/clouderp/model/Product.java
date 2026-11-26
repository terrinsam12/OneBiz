package com.clouderp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "ix_products_company_id", columnList = "company_id")
})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "cost_price", nullable = false)
    private Double costPrice = 0.0;

    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel = 5;

    public Product() {
    }

    public Product(Long companyId, String name, Double price, Integer quantity, Double costPrice, Integer reorderLevel) {
        this.companyId = companyId;
        this.name = name;
        this.price = price;
        this.quantity = quantity != null ? quantity : 0;
        this.costPrice = costPrice != null ? costPrice : 0.0;
        this.reorderLevel = reorderLevel != null ? reorderLevel : 5;
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
