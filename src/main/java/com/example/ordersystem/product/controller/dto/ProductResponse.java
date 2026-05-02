package com.example.ordersystem.product.controller.dto;

import com.example.ordersystem.product.domain.Category;
import com.example.ordersystem.product.domain.Product;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProductResponse {

    private final Long id;
    private final String name;
    private final long price;
    private final int stockQuantity;
    private final Category category;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ProductResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
        this.category = product.getCategory();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
    }

    public static ProductResponse from(Product product) {
        return new ProductResponse(product);
    }
}
