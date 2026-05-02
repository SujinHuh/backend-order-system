package com.example.ordersystem.product.domain;

import com.example.ordersystem.global.error.exception.BusinessException;
import com.example.ordersystem.global.error.exception.InvalidValueException;
import com.example.ordersystem.product.exception.NotEnoughStockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    @DisplayName("상품 가격을 변경한다.")
    void changePrice() {
        // given
        Product product = createProduct(1000L, 10);

        // when
        product.changePrice(2000L);

        // then
        assertThat(product.getPrice()).isEqualTo(2000L);
    }

    @Test
    @DisplayName("상품 가격을 음수로 변경하면 예외가 발생한다.")
    void changePrice_negative() {
        // given
        Product product = createProduct(1000L, 10);

        // when & then
        assertThatThrownBy(() -> product.changePrice(-1L))
                .isInstanceOf(InvalidValueException.class);
    }

    @Test
    @DisplayName("재고를 추가한다.")
    void addStock() {
        // given
        Product product = createProduct(1000L, 10);

        // when
        product.addStock(5);

        // then
        assertThat(product.getStockQuantity()).isEqualTo(15);
    }

    @Test
    @DisplayName("재고를 차감한다.")
    void removeStock() {
        // given
        Product product = createProduct(1000L, 10);

        // when
        product.removeStock(5);

        // then
        assertThat(product.getStockQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("남은 재고보다 많이 차감하면 예외가 발생한다.")
    void removeStock_notEnough() {
        // given
        Product product = createProduct(1000L, 10);

        // when & then
        assertThatThrownBy(() -> product.removeStock(11))
                .isInstanceOf(NotEnoughStockException.class)
                .hasMessageContaining("Not enough stock");
    }

    private Product createProduct(long price, int stockQuantity) {
        return Product.builder()
                .name("Test Product")
                .price(price)
                .stockQuantity(stockQuantity)
                .category(Category.FOOD)
                .build();
    }
}
