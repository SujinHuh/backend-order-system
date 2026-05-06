package com.example.ordersystem.product.domain;

import com.example.ordersystem.global.error.exception.InvalidValueException;
import com.example.ordersystem.product.exception.NotEnoughStockException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private long price;

    @Column(nullable = false)
    private int stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Product(String name, long price, int stockQuantity, Category category) {
        validatePrice(price);
        validateStock(stockQuantity);
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
    }

    public void update(String name, long price, int stockQuantity, Category category) {
        validatePrice(price);
        validateStock(stockQuantity);
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
    }

    public void changePrice(long price) {
        validatePrice(price);
        this.price = price;
    }

    public void addStock(int quantity) {
        validateQuantity(quantity);
        this.stockQuantity += quantity;
    }

    public void removeStock(int quantity) {
        validateQuantity(quantity);
        if (this.stockQuantity < quantity) {
            throw new NotEnoughStockException("Not enough stock. Current stock: " + this.stockQuantity);
        }
        this.stockQuantity -= quantity;
    }

    private void validatePrice(long price) {
        if (price < 0) {
            throw new InvalidValueException(String.valueOf(price));
        }
    }

    private void validateStock(int stock) {
        if (stock < 0) {
            throw new InvalidValueException(String.valueOf(stock));
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new InvalidValueException(String.valueOf(quantity));
        }
    }
}
