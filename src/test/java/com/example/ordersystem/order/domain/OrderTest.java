package com.example.ordersystem.order.domain;

import com.example.ordersystem.global.error.exception.InvalidValueException;
import com.example.ordersystem.product.domain.Category;
import com.example.ordersystem.product.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    @DisplayName("주문 생성 시 초기 상태는 PENDING이다.")
    void createOrder_initialStatus() {
        // when
        Order order = Order.createOrder();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("주문에 항목을 추가하면 총액이 올바르게 계산된다.")
    void order_totalPriceCalculation() {
        // given
        Product product1 = createProduct(1L, "Product 1", 1000L);
        Product product2 = createProduct(2L, "Product 2", 2000L);

        Order order = Order.createOrder();
        order.addOrderItem(product1, 2); // 1000 * 2 = 2000
        order.addOrderItem(product2, 1); // 2000 * 1 = 2000

        // when
        long totalAmount = order.getTotalAmount();

        // then
        assertThat(totalAmount).isEqualTo(4000L);
        assertThat(order.getOrderItems()).hasSize(2);
    }

    @Test
    @DisplayName("주문 항목의 수량이 1보다 작으면 예외가 발생한다.")
    void orderItem_invalidQuantity() {
        // given
        Product product = createProduct(1L, "Product", 1000L);
        Order order = Order.createOrder();

        // when & then
        assertThatThrownBy(() -> order.addOrderItem(product, 0))
                .isInstanceOf(InvalidValueException.class);
    }

    private Product createProduct(Long id, String name, long price) {
        Product product = Product.builder()
                .name(name)
                .price(price)
                .stockQuantity(100)
                .category(Category.FOOD)
                .build();
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
