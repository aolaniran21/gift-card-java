package com.example.orders.application;

import com.example.orders.api.CreateOrderRequest;
import com.example.orders.api.OrderResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final CreateOrderUseCase createOrderUseCase;
    private final ListOrdersUseCase listOrdersUseCase;

    public OrderService(CreateOrderUseCase createOrderUseCase, ListOrdersUseCase listOrdersUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.listOrdersUseCase = listOrdersUseCase;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        return createOrderUseCase.execute(request);
    }

    public List<OrderResponse> listOrders() {
        return listOrdersUseCase.execute();
    }
}
