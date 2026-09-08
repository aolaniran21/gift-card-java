package com.example.orders.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {

    private Long id;
    private String customerName;
    private String product;
    private BigDecimal total;
    private String status = "PENDING";
    private LocalDateTime createdAt = LocalDateTime.now();

    protected Order() {
    }

    public Order(Long id, String customerName, String product, BigDecimal total, String status, LocalDateTime createdAt) {
        this.id = id;
        this.customerName = customerName;
        this.product = product;
        this.total = total;
        this.status = status;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public Order(String customerName, String product, BigDecimal total) {
        this(null, customerName, product, total, "PENDING", LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) { this.id = id; }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
