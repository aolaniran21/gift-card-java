package com.example.orders.application;

import com.example.orders.api.CreateOrderRequest;
import com.example.orders.api.OrderResponse;
import com.example.orders.domain.Order;
import com.example.orders.domain.OrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;

    public CreateOrderUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse execute(CreateOrderRequest request) {
        Order order = new Order(request.customerName(), request.product(), request.total());
        Order saved = orderRepository.save(order);
        return new OrderResponse(saved.getId(), saved.getCustomerName(), saved.getProduct(), saved.getTotal(), saved.getStatus(), saved.getCreatedAt());
    }
}
