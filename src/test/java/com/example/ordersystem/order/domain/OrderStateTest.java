package com.example.ordersystem.order.domain;

import com.example.ordersystem.global.error.exception.BusinessException;
import com.example.ordersystem.product.domain.Category;
import com.example.ordersystem.product.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStateTest {

    private Order order;
    private Product product;

    @BeforeEach
    void setUp() {
        order = Order.createOrder();
        product = Product.builder()
                .name("Test Product")
                .price(1000L)
                .stockQuantity(10)
                .category(Category.FOOD)
                .build();
        ReflectionTestUtils.setField(product, "id", 1L);
        order.addOrderItem(product, 2);
    }

    @Test
    @DisplayName("PENDING 상태에서 ACCEPTED로 전이할 수 있다.")
    void transition_pending_to_accepted() {
        // when
        order.changeStatus(OrderStatus.ACCEPTED);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(product.getStockQuantity()).isEqualTo(10); // 재고 변화 없음
    }

    @Test
    @DisplayName("ACCEPTED 상태에서 COMPLETED로 전이 시 재고가 차감된다.")
    void transition_accepted_to_completed_deducts_stock() {
        // given
        order.changeStatus(OrderStatus.ACCEPTED);

        // when
        order.changeStatus(OrderStatus.COMPLETED);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(product.getStockQuantity()).isEqualTo(8); // 10 - 2 = 8
    }

    @Test
    @DisplayName("COMPLETED 상태에서 CANCELED로 전이 시 재고가 복구된다.")
    void transition_completed_to_canceled_restores_stock() {
        // given
        order.changeStatus(OrderStatus.ACCEPTED);
        order.changeStatus(OrderStatus.COMPLETED); // 재고 8
        assertThat(product.getStockQuantity()).isEqualTo(8);

        // when
        order.changeStatus(OrderStatus.CANCELED);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(product.getStockQuantity()).isEqualTo(10); // 8 + 2 = 10
    }

    @Test
    @DisplayName("ACCEPTED 상태에서 CANCELED로 전이 시 재고는 변하지 않는다.")
    void transition_accepted_to_canceled_no_stock_change() {
        // given
        order.changeStatus(OrderStatus.ACCEPTED);

        // when
        order.changeStatus(OrderStatus.CANCELED);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(product.getStockQuantity()).isEqualTo(10); // 변화 없음
    }

    @Test
    @DisplayName("PENDING 상태에서 CANCELED로 전이할 수 있다.")
    void transition_pending_to_canceled() {
        // when
        order.changeStatus(OrderStatus.CANCELED);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(product.getStockQuantity()).isEqualTo(10); // 재고 변화 없음
    }

    @Test
    @DisplayName("이미 CANCELED인 주문은 상태를 변경할 수 없다.")
    void transition_from_canceled_fails() {
        // given
        order.changeStatus(OrderStatus.CANCELED);

        // when & then
        assertThatThrownBy(() -> order.changeStatus(OrderStatus.ACCEPTED))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("재고가 부족하면 COMPLETED로의 전이가 실패하고 예외가 발생한다.")
    void transition_to_completed_fails_if_stock_insufficient() {
        // given
        order.changeStatus(OrderStatus.ACCEPTED);
        product.removeStock(9); // 남은 재고 1, 주문 수량 2
        
        // when & then
        assertThatThrownBy(() -> order.changeStatus(OrderStatus.COMPLETED))
                .isInstanceOf(com.example.ordersystem.product.exception.NotEnoughStockException.class);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ACCEPTED); // 상태 원복 확인
    }

    @Test
    @DisplayName("동일한 상태로의 변경 요청은 예외가 발생한다.")
    void transition_to_same_status_fails() {
        // when & then
        assertThatThrownBy(() -> order.changeStatus(OrderStatus.PENDING))
                .isInstanceOf(BusinessException.class);
    }
}
