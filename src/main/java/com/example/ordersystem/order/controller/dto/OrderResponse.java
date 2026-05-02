package com.example.ordersystem.order.controller.dto;

import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.domain.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OrderResponse {

    private final Long id;
    private final OrderStatus status;
    private final long totalAmount;
    private final List<OrderItemResponse> orderItems;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private OrderResponse(Order order) {
        this.id = order.getId();
        this.status = order.getStatus();
        this.totalAmount = order.getTotalAmount();
        this.orderItems = order.getOrderItems().stream()
                .map(OrderItemResponse::from)
                .collect(Collectors.toList());
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();
    }

    public static OrderResponse from(Order order) {
        return new OrderResponse(order);
    }

    @Getter
    public static class OrderItemResponse {
        private final Long productId;
        private final String productName;
        private final int quantity;
        private final long orderPrice;

        private OrderItemResponse(com.example.ordersystem.order.domain.OrderItem orderItem) {
            this.productId = orderItem.getProduct().getId();
            this.productName = orderItem.getProduct().getName();
            this.quantity = orderItem.getQuantity();
            this.orderPrice = orderItem.getOrderPrice();
        }

        public static OrderItemResponse from(com.example.ordersystem.order.domain.OrderItem orderItem) {
            return new OrderItemResponse(orderItem);
        }
    }
}
