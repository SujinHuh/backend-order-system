package com.example.ordersystem.order.service;

import com.example.ordersystem.order.controller.dto.OrderResponse;
import com.example.ordersystem.global.error.exception.EntityNotFoundException;
import com.example.ordersystem.order.domain.Order;
import com.example.ordersystem.order.domain.OrderRepository;
import com.example.ordersystem.order.domain.OrderStatus;
import com.example.ordersystem.product.domain.Category;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.domain.ProductRepository;
import com.example.ordersystem.product.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("주문 요청을 받아 주문을 생성하고 저장한다.")
    void createOrder() {
        // given
        Product product1 = createProduct(1L, "Product 1", 1000L);
        Product product2 = createProduct(2L, "Product 2", 2000L);

        given(productService.getProduct(1L)).willReturn(product1);
        given(productService.getProduct(2L)).willReturn(product2);

        Order order = Order.createOrder();
        order.addOrderItem(product1, 2);
        order.addOrderItem(product2, 1);
        ReflectionTestUtils.setField(order, "id", 1L);
        given(orderRepository.save(any(Order.class))).willReturn(order);

        List<OrderService.OrderItemRequest> items = List.of(
                new OrderService.OrderItemRequest(1L, 2),
                new OrderService.OrderItemRequest(2L, 1)
        );

        // when
        OrderResponse result = orderService.createOrder(items);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        verify(productService, times(2)).getProduct(any());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("존재하지 않는 상품 ID로 주문하면 예외가 발생한다.")
    void createOrder_productNotFound() {
        // given
        given(productService.getProduct(999L)).willThrow(new EntityNotFoundException("Product not found"));
        List<OrderService.OrderItemRequest> items = List.of(new OrderService.OrderItemRequest(999L, 1));

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(items))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("주문 상태를 변경한다.")
    void updateStatus() {
        // given
        Product product = createProduct(1L, "Product", 1000L);
        Order order = Order.createOrder();
        order.addOrderItem(product, 1);
        given(orderRepository.findByIdForUpdate(1L)).willReturn(Optional.of(order));

        // when
        OrderResponse response = orderService.updateStatus(1L, OrderStatus.ACCEPTED);

        // then
        assertThat(response.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
        verify(orderRepository).findByIdForUpdate(1L);
    }

    @Test
    @DisplayName("존재하지 않는 주문의 상태를 변경하면 예외가 발생한다.")
    void updateStatus_orderNotFound() {
        // given
        given(orderRepository.findByIdForUpdate(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.updateStatus(999L, OrderStatus.ACCEPTED))
                .isInstanceOf(EntityNotFoundException.class);
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
