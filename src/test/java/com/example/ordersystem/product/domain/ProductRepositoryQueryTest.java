package com.example.ordersystem.product.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductRepositoryQueryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("카테고리 조건이 있으면 해당 카테고리 상품만 페이징 조회한다.")
    void searchByCategory() {
        Product food = productRepository.save(createProduct("Food", Category.FOOD));
        productRepository.save(createProduct("Book", Category.BOOK));

        Page<Product> result = productRepository.search(Category.FOOD, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Product::getId)
                .containsExactly(food.getId());
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("카테고리 조건이 없으면 전체 상품을 페이징 조회한다.")
    void searchWithoutCategory() {
        productRepository.save(createProduct("Food", Category.FOOD));
        productRepository.save(createProduct("Book", Category.BOOK));

        Page<Product> result = productRepository.search(null, PageRequest.of(0, 1));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("요청 페이지가 마지막 페이지를 넘어도 전체 상품 수를 유지한다.")
    void searchOutOfRangePageKeepsTotalCount() {
        productRepository.save(createProduct("Food", Category.FOOD));
        productRepository.save(createProduct("Book", Category.BOOK));

        Page<Product> result = productRepository.search(null, PageRequest.of(2, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    private Product createProduct(String name, Category category) {
        return Product.builder()
                .name(name)
                .price(1000L)
                .stockQuantity(10)
                .category(category)
                .build();
    }
}
