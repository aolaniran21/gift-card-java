package com.example.orders.application;

import com.example.orders.api.OrderResponse;
import com.example.orders.domain.OrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ListOrdersUseCase {

    private final OrderRepository orderRepository;

    public ListOrdersUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> execute() {
        return orderRepository.findAll().stream()
                .map(order -> new OrderResponse(order.getId(), order.getCustomerName(), order.getProduct(), order.getTotal(), order.getStatus(), order.getCreatedAt()))
                .toList();
    }
}
