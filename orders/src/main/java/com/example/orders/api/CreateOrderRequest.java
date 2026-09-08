package com.example.orders.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotBlank(message = "Customer name is required") String customerName,
        @NotBlank(message = "Product is required") String product,
        @NotNull(message = "Total is required") @Positive(message = "Total must be positive") BigDecimal total
) {
}
