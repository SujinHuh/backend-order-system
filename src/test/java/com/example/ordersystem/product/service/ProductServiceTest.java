package com.example.ordersystem.product.service;

import com.example.ordersystem.product.domain.Category;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.domain.ProductRepository;
import com.example.ordersystem.global.error.exception.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("상품을 등록한다.")
    void createProduct() {
        // given
        Product product = Product.builder()
                .name("Product A")
                .price(1000L)
                .stockQuantity(10)
                .category(Category.FOOD)
                .build();
        ReflectionTestUtils.setField(product, "id", 1L);
        given(productRepository.save(any(Product.class))).willReturn(product);

        // when
        Product result = productService.createProduct("Product A", 1000L, 10, Category.FOOD);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Product A");
    }

    @Test
    @DisplayName("상품 정보를 수정한다.")
    void updateProduct() {
        // given
        Product product = Product.builder()
                .name("Product A")
                .price(1000L)
                .stockQuantity(10)
                .category(Category.FOOD)
                .build();
        ReflectionTestUtils.setField(product, "id", 1L);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));

        // when
        Product result = productService.updateProduct(1L, "Updated Product", 2000L, 20, Category.FASHION);

        // then
        assertThat(result.getName()).isEqualTo("Updated Product");
        assertThat(result.getPrice()).isEqualTo(2000L);
    }

    @Test
    @DisplayName("상품을 단건 조회한다.")
    void getProduct() {
        // given
        Product product = Product.builder()
                .name("Product A")
                .price(1000L)
                .stockQuantity(10)
                .category(Category.FOOD)
                .build();
        ReflectionTestUtils.setField(product, "id", 1L);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));

        // when
        Product result = productService.getProduct(1L);

        // then
        assertThat(result.getName()).isEqualTo("Product A");
    }

    @Test
    @DisplayName("상품 목록을 페이징 조회한다.")
    void getProducts() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Product product = Product.builder()
                .name("Product A")
                .price(1000L)
                .stockQuantity(10)
                .category(Category.FOOD)
                .build();
        ReflectionTestUtils.setField(product, "id", 1L);
        Page<Product> page = new PageImpl<>(List.of(product));
        given(productRepository.findAll(pageable)).willReturn(page);

        // when
        Page<Product> result = productService.getProducts(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
    }
}
