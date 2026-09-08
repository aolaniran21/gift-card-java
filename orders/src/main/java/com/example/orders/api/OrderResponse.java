package com.example.orders.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(Long id, String customerName, String product, BigDecimal total, String status, LocalDateTime createdAt) {
}
