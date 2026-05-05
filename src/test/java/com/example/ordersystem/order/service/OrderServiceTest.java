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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
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

    @Test
    @DisplayName("주문 단건을 주문 항목과 상품 정보 조회 경로로 조회한다.")
    void getOrder() {
        // given
        Product product = createProduct(1L, "Product", 1000L);
        Order order = Order.createOrder();
        order.addOrderItem(product, 1);
        ReflectionTestUtils.setField(order, "id", 1L);
        given(orderRepository.findByIdWithItemsAndProducts(1L)).willReturn(Optional.of(order));

        // when
        OrderResponse response = orderService.getOrder(1L);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getOrderItems()).hasSize(1);
        assertThat(response.getOrderItems().get(0).getProductName()).isEqualTo("Product");
        verify(orderRepository).findByIdWithItemsAndProducts(1L);
    }

    @Test
    @DisplayName("존재하지 않는 주문을 단건 조회하면 예외가 발생한다.")
    void getOrder_notFound() {
        // given
        given(orderRepository.findByIdWithItemsAndProducts(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrder(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("주문 목록을 상태와 기간 조건으로 페이징 조회한다.")
    void getOrders() {
        // given
        Product product = createProduct(1L, "Product", 1000L);
        Order order = Order.createOrder();
        order.addOrderItem(product, 1);
        ReflectionTestUtils.setField(order, "id", 1L);
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 1, 31, 23, 59);
        given(orderRepository.search(OrderStatus.PENDING, from, to, pageable))
                .willReturn(new PageImpl<>(List.of(order), pageable, 1));

        // when
        Page<OrderResponse> result = orderService.getOrders(OrderStatus.PENDING, from, to, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getOrderItems().get(0).getProductName()).isEqualTo("Product");
        verify(orderRepository).search(OrderStatus.PENDING, from, to, pageable);
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
