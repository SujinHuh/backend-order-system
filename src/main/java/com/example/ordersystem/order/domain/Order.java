package com.example.ordersystem.order.domain;

import com.example.ordersystem.global.error.ErrorCode;
import com.example.ordersystem.global.error.exception.BusinessException;
import com.example.ordersystem.product.domain.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private Order(OrderStatus status) {
        this.status = status;
    }

    public static Order createOrder() {
        return new Order(OrderStatus.PENDING);
    }

    public void addOrderItem(Product product, int quantity) {
        OrderItem orderItem = OrderItem.createOrderItem(this, product, quantity);
        this.orderItems.add(orderItem);
    }

    public void changeStatus(OrderStatus targetStatus) {
        validateTransition(targetStatus);

        if (targetStatus == OrderStatus.COMPLETED) {
            orderItems.forEach(OrderItem::deductStock);
        }

        if (targetStatus == OrderStatus.CANCELED && this.status == OrderStatus.COMPLETED) {
            orderItems.forEach(OrderItem::restoreStock);
        }

        this.status = targetStatus;
    }

    public boolean requiresStockLock(OrderStatus targetStatus) {
        return (this.status == OrderStatus.ACCEPTED && targetStatus == OrderStatus.COMPLETED)
                || (this.status == OrderStatus.COMPLETED && targetStatus == OrderStatus.CANCELED);
    }

    public List<Long> getOrderItemProductIds() {
        return orderItems.stream()
                .map(OrderItem::getProductId)
                .distinct()
                .sorted()
                .toList();
    }

    private void validateTransition(OrderStatus targetStatus) {
        if (this.status == targetStatus) {
            throw new BusinessException("Same status transition is not allowed", ErrorCode.INVALID_INPUT_VALUE);
        }

        if (this.status == OrderStatus.CANCELED) {
            throw new BusinessException("Cannot change status from CANCELED", ErrorCode.INVALID_INPUT_VALUE);
        }

        boolean isValid = switch (this.status) {
            case PENDING -> targetStatus == OrderStatus.ACCEPTED || targetStatus == OrderStatus.CANCELED;
            case ACCEPTED -> targetStatus == OrderStatus.COMPLETED || targetStatus == OrderStatus.CANCELED;
            case COMPLETED -> targetStatus == OrderStatus.CANCELED;
            default -> false;
        };

        if (!isValid) {
            throw new BusinessException("Invalid status transition: " + this.status + " -> " + targetStatus, ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    public long getTotalAmount() {
        return orderItems.stream()
                .mapToLong(OrderItem::getTotalPrice)
                .sum();
    }
}
