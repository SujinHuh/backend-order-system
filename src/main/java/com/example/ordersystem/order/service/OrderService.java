package com.example.ordersystem.order.service;

import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.domain.OrderRepository;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Transactional
    public Order createOrder(List<OrderItemRequest> itemRequests) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            throw new com.example.ordersystem.global.error.exception.InvalidValueException("Order items cannot be empty");
        }
        Order order = Order.createOrder();

        for (OrderItemRequest itemRequest : itemRequests) {
            Product product = productService.getProduct(itemRequest.getProductId());
            order.addOrderItem(product, itemRequest.getQuantity());
        }

        return orderRepository.save(order);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        private Long productId;
        private int quantity;
    }
}
