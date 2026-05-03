package com.example.ordersystem.product.service;

import com.example.ordersystem.product.domain.Category;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.domain.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("상품을 실제 DB에 저장하고 조회한다.")
    void createAndGetProduct_withDatabase() {
        // when
        Product savedProduct = productService.createProduct("Product A", 1000L, 10, Category.FOOD);

        // then
        Product foundProduct = productService.getProduct(savedProduct.getId());
        assertThat(foundProduct.getId()).isNotNull();
        assertThat(foundProduct.getName()).isEqualTo("Product A");
        assertThat(foundProduct.getPrice()).isEqualTo(1000L);
        assertThat(foundProduct.getStockQuantity()).isEqualTo(10);
        assertThat(foundProduct.getCategory()).isEqualTo(Category.FOOD);
        assertThat(foundProduct.getCreatedAt()).isNotNull();
        assertThat(foundProduct.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("상품 정보를 실제 DB에 수정 반영한다.")
    void updateProduct_withDatabase() {
        // given
        Product savedProduct = productRepository.save(Product.builder()
                .name("Product A")
                .price(1000L)
                .stockQuantity(10)
                .category(Category.FOOD)
                .build());

        // when
        Product updatedProduct = productService.updateProduct(
                savedProduct.getId(),
                "Updated Product",
                2000L,
                20,
                Category.FASHION
        );

        // then
        assertThat(updatedProduct.getName()).isEqualTo("Updated Product");
        assertThat(updatedProduct.getPrice()).isEqualTo(2000L);
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(20);
        assertThat(updatedProduct.getCategory()).isEqualTo(Category.FASHION);
    }
}
