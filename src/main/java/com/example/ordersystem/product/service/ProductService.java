package com.example.ordersystem.product.service;

import com.example.ordersystem.product.domain.Category;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.domain.ProductRepository;
import com.example.ordersystem.global.error.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Product createProduct(String name, long price, int stockQuantity, Category category) {
        Product product = Product.builder()
                .name(name)
                .price(price)
                .stockQuantity(stockQuantity)
                .category(category)
                .build();
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, String name, long price, int stockQuantity, Category category) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        
        product.update(name, price, stockQuantity, category);
        return product;
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    public Page<Product> getProducts(Pageable pageable) {
        return getProducts(null, pageable);
    }

    public Page<Product> getProducts(Category category, Pageable pageable) {
        return productRepository.search(category, pageable);
    }
}
