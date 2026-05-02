package com.example.ordersystem.order.domain;

import com.example.ordersystem.global.error.exception.InvalidValueException;
import com.example.ordersystem.product.domain.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private long orderPrice;

    private OrderItem(Order order, Product product, int quantity, long orderPrice) {
        validateQuantity(quantity);
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
    }

    public static OrderItem createOrderItem(Order order, Product product, int quantity) {
        return new OrderItem(order, product, quantity, product.getPrice());
    }

    public long getTotalPrice() {
        return orderPrice * quantity;
    }

    private void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new InvalidValueException(String.valueOf(quantity));
        }
    }
}
