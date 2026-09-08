package com.example.orders.infrastructure.persistence;

import com.example.orders.domain.Order;
import com.example.orders.domain.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderRepositoryAdapter implements OrderRepository {

    private final JpaOrderRepository jpa;

    public OrderRepositoryAdapter(JpaOrderRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = toEntity(order);
        OrderEntity saved = jpa.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Order> findAll() {
        return jpa.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    private Order toDomain(OrderEntity e) {
        return new Order(e.getId(), e.getCustomerName(), e.getProduct(), e.getTotal(), e.getStatus(), e.getCreatedAt());
    }

    private OrderEntity toEntity(Order o) {
        OrderEntity e = new OrderEntity(o.getCustomerName(), o.getProduct(), o.getTotal());
        if (o.getId() != null) {
            e.setId(o.getId());
        }
        e.setStatus(o.getStatus());
        e.setCreatedAt(o.getCreatedAt());
        return e;
    }
}
